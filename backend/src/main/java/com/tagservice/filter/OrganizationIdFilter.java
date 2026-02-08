package com.tagservice.filter;

import com.tagservice.enums.ApiErrorType;
import com.tagservice.util.ErrorResponseUtil;
import com.tagservice.util.MDCUtil;
import com.tagservice.util.ValidationUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to validate the presence and format of the X-Organization-Id header.
 * This filter runs after the RequestIdFilter to ensure organization context is
 * available
 * for all requests (except health check endpoints).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
@RequiredArgsConstructor
public class OrganizationIdFilter implements Filter {

    private static final String ORGANIZATION_ID_HEADER = "X-Organization-Id";

    private final ErrorResponseUtil errorResponseUtil;

    // Paths that don't require organization ID
    private static final String[] EXCLUDED_PATHS = {
            "/actuator",
            "/health"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();
        String requestMethod = httpRequest.getMethod();

        if (isExcludedPath(requestPath) || isOrganizationCreationRequest(requestPath, requestMethod)) {
            log.debug("Skipping organization ID validation for excluded path: {}", requestPath, requestMethod);
            chain.doFilter(request, response);
            return;
        }

        String organizationId = httpRequest.getHeader(ORGANIZATION_ID_HEADER);

        if (StringUtils.isBlank(organizationId)) {
            log.warn("Missing {} header for request: {}", ORGANIZATION_ID_HEADER, requestPath);
            errorResponseUtil.sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiErrorType.MISSING_HEADER,
                    "The request is missing the required '" + ORGANIZATION_ID_HEADER + "' header.",
                    requestPath);
            return;
        }

        if (!ValidationUtils.isValidUUID(organizationId)) {
            log.warn("Invalid {} header format: {} for request: {}",
                    ORGANIZATION_ID_HEADER, organizationId, requestPath);
            errorResponseUtil.sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiErrorType.INVALID_HEADER,
                    "The '" + ORGANIZATION_ID_HEADER + "' header must be a valid UUID.",
                    requestPath);
            return;
        }

        log.debug("Valid organization ID: {} for request: {}", organizationId, requestPath);
        MDCUtil.runWithOrganizationId((MDCUtil.FilterRunnable) () -> {
            chain.doFilter(request, response);
        }, organizationId);
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
}
