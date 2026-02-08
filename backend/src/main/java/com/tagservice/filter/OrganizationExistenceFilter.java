package com.tagservice.filter;

import com.tagservice.client.OrganizationClient;
import com.tagservice.context.OrganizationContext;
import com.tagservice.enums.ApiErrorType;
import com.tagservice.dto.error.OrganizationDto;
import com.tagservice.util.ErrorResponseUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter that ensures the organization referenced in the request context exists
 * and is not soft-deleted.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class OrganizationExistenceFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationExistenceFilter.class);

    private static final String[] EXCLUDED_PATHS = {
            "/actuator",
            "/health"
    };

    private final OrganizationClient organizationClient;
    private final ErrorResponseUtil errorResponseUtil;

    public OrganizationExistenceFilter(OrganizationClient organizationClient,
                                       ErrorResponseUtil errorResponseUtil) {
        this.organizationClient = organizationClient;
        this.errorResponseUtil = errorResponseUtil;
    }

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

        String organizationIdFromContext = OrganizationContext.getOrganizationId();

        // If no organization has been set in the context (e.g., for routes that don't require it),
        // skip validation and continue the filter chain.
        if (organizationIdFromContext == null) {
            logger.debug("No organization in context for request: {}, skipping existence validation", requestPath);
            chain.doFilter(request, response);
            return;
        }

        Long organizationId;
        try {
            organizationId = Long.valueOf(organizationIdFromContext);
        } catch (NumberFormatException ex) {
            logger.warn("Invalid organization ID in context: {} for request: {}",
                    organizationIdFromContext, requestPath);
            errorResponseUtil.sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiErrorType.INVALID_ORGANIZATION_ID,
                    "The organization identifier is not a valid numeric ID.",
                    requestPath);
            return;
        }

        try {
            OrganizationDto organization = organizationClient.getActiveOrganizationById(organizationId);

            // Defensive check: service already treats soft-deleted as non-existent,
            // but we keep this in case the implementation changes.
            if (organization.getDeletedAt() != null) {
                logger.warn("Organization {} is soft-deleted for request: {}", organizationId, requestPath);
                errorResponseUtil.sendErrorResponse(httpResponse,
                        HttpServletResponse.SC_GONE,
                        ApiErrorType.ORGANIZATION_DELETED,
                        "The organization associated with this request has been deleted.",
                        requestPath);
                return;
            }

        } catch (EntityNotFoundException ex) {
            logger.warn("Organization not found with id {} for request: {}", organizationId, requestPath);
            errorResponseUtil.sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_NOT_FOUND,
                    ApiErrorType.ORGANIZATION_NOT_FOUND,
                    "The organization associated with this request does not exist or has been deleted.",
                    requestPath);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOrganizationCreationRequest(String path, String method) {
        return "POST".equalsIgnoreCase(method) && path.matches("^/api/v\\d+/organizations/?$");
    }
}

