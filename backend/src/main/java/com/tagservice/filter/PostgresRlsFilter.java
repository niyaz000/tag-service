package com.tagservice.filter;

import com.tagservice.context.OrganizationContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to set the Row-Level Security (RLS) context in PostgreSQL.
 * This should run after the OrganizationIdFilter so that the tenant ID is
 * available.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class PostgresRlsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(PostgresRlsFilter.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Paths that should be excluded from RLS (consistent with OrganizationIdFilter)
    private static final String[] EXCLUDED_PATHS = {
            "/actuator",
            "/api/v1/health"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestPath = httpRequest.getRequestURI();

        if (isExcludedPath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        String organizationId = OrganizationContext.getOrganizationId();

        if (organizationId != null) {
            try {
                // Set the session variable for PostgreSQL RLS.
                // We use 'SET' here because the filter is likely outside a transaction.
                // However, 'SET LOCAL' is preferred within a transaction.
                // Note: The organization ID is expected to be an INTEGER in the DB policies
                // based on migration files, but we are receiving it as a UUID string.
                // We might need to map it or use the string directly if policies allow.
                // For now, we set it as a string context variable.

                logger.debug("Setting PostgreSQL RLS context for organization: {}", organizationId);

                // Using a string-based setting 'app.current_tenant_id'
                // This will be accessible via current_setting('app.current_tenant_id')
                jdbcTemplate.execute(String.format("SET app.current_tenant_id = '%s'", organizationId));

                chain.doFilter(request, response);

            } finally {
                // Clear the session variable to prevent leakage back to the connection pool
                try {
                    jdbcTemplate.execute("RESET app.current_tenant_id");
                } catch (Exception e) {
                    logger.warn("Failed to reset app.current_tenant_id", e);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isExcludedPath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}
