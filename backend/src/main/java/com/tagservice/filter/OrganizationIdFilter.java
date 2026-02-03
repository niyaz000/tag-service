package com.tagservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tagservice.dto.error.ErrorResponse;
import com.tagservice.util.ValidationUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Filter to validate the presence and format of the X-Organization-Id header.
 * This filter runs after the RequestIdFilter to ensure organization context is
 * available
 * for all requests (except health check endpoints).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class OrganizationIdFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationIdFilter.class);
    private static final String ORGANIZATION_ID_HEADER = "X-Organization-Id";
    private static final String MDC_ORGANIZATION_ID_KEY = "organizationId";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Autowired
    private ObjectMapper objectMapper;

    // Paths that don't require organization ID
    private static final String[] EXCLUDED_PATHS = {
            "/actuator",
            "/api/v1/health"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();
        String requestMethod = httpRequest.getMethod();

        // Skip validation for excluded paths or POST to organizations endpoint
        if (isExcludedPath(requestPath) || isOrganizationCreationRequest(requestPath, requestMethod)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String organizationId = httpRequest.getHeader(ORGANIZATION_ID_HEADER);

            // Validate organization ID presence
            if (StringUtils.isBlank(organizationId)) {
                logger.warn("Missing {} header for request: {}", ORGANIZATION_ID_HEADER, requestPath);
                sendErrorResponse(httpResponse, HttpServletResponse.SC_BAD_REQUEST,
                        "https://api.tag-service.com/errors#missing-header",
                        "Missing Required Header",
                        "The request is missing the required '" + ORGANIZATION_ID_HEADER + "' header.",
                        requestPath);
                return;
            }

            // Validate organization ID format (must be a valid UUID)
            if (!ValidationUtils.isValidUUID(organizationId)) {
                logger.warn("Invalid {} header format: {} for request: {}",
                        ORGANIZATION_ID_HEADER, organizationId, requestPath);
                sendErrorResponse(httpResponse, HttpServletResponse.SC_BAD_REQUEST,
                        "https://api.tag-service.com/errors#invalid-header",
                        "Invalid Header Format",
                        "The '" + ORGANIZATION_ID_HEADER + "' header must be a valid UUID.",
                        requestPath);
                return;
            }

            // Add organization ID to MDC for logging
            MDC.put(MDC_ORGANIZATION_ID_KEY, organizationId);

            // Continue with the filter chain
            chain.doFilter(request, response);

        } finally {
            // Clean up MDC
            MDC.remove(MDC_ORGANIZATION_ID_KEY);
        }
    }

    /**
     * Checks if the request path should be excluded from organization ID
     * validation.
     *
     * @param path the request path
     * @return true if the path should be excluded, false otherwise
     */
    private boolean isExcludedPath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the request is a POST to create a new organization.
     *
     * @param path   the request path
     * @param method the HTTP method
     * @return true if this is an organization creation request, false otherwise
     */
    private boolean isOrganizationCreationRequest(String path, String method) {
        return "POST".equalsIgnoreCase(method) && path.matches("^/api/v\\d+/organizations/?$");
    }

    /**
     * Sends a JSON error response following the project's standard error format.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String type,
            String title, String detail, String instance)
            throws IOException {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(type)
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .request_id(MDC.get(MDC_REQUEST_ID_KEY))
                .timestamp(OffsetDateTime.now())
                .build();

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
