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
            String requestId = resolveRequestId(httpRequest);
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private String resolveRequestId(HttpServletRequest httpRequest) {
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.isNotBlank(requestId) && ValidationUtils.isValidUUID(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }
}
