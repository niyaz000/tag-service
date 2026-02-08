package com.tagservice.filter;

import com.tagservice.util.MDCUtil;
import com.tagservice.util.ValidationUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestId = resolveRequestId(httpRequest);
        log.debug("Request ID: {} for request: {}", requestId, httpRequest.getRequestURI());
        MDCUtil.runWithRequestId((MDCUtil.FilterRunnable) () -> {
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
            chain.doFilter(request, response);
        }, requestId);
    }

    /**
     * Resolves the request ID from the request header.
     * If the request ID is not present or invalid, a new random UUID is generated.
     *
     * @param httpRequest the HTTP request
     * @return the request ID
     */
    private String resolveRequestId(HttpServletRequest httpRequest) {
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.isNotBlank(requestId) && ValidationUtils.isValidUUID(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }
}
