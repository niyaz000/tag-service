package com.tagservice.filter;

import com.tagservice.util.ValidationUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add a unique request ID to every HTTP request/response.
 * This filter runs at the highest order to ensure the request ID is available
 * throughout the entire request lifecycle.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Check if request already has a request ID (from client or proxy)
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);

            // Validate and use client-provided request ID if it's a valid UUID
            if (StringUtils.isNotBlank(requestId)) {
                if (!ValidationUtils.isValidUUID(requestId)) {
                    // Invalid UUID format, generate a new one
                    requestId = UUID.randomUUID().toString();
                }
            } else {
                // No request ID provided, generate a new UUID
                requestId = UUID.randomUUID().toString();
            }

            // Add request ID to MDC for logging
            MDC.put(MDC_REQUEST_ID_KEY, requestId);

            // Add request ID to response headers
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

            // Continue with the filter chain
            chain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }
}
