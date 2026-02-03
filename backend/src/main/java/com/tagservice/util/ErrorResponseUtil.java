package com.tagservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tagservice.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Utility class for sending standardized error responses.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ErrorResponseUtil {

    private final ObjectMapper objectMapper;

    /**
     * Sends a JSON error response following the project's standard error format.
     *
     * @param response the HTTP response
     * @param status   the HTTP status code
     * @param type     the error type URI (RFC 7807)
     * @param title    the error title
     * @param detail   the error detail message
     * @param instance the request path/instance URI
     */
    public void sendErrorResponse(HttpServletResponse response, int status, String type,
            String title, String detail, String instance) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(type)
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .request_id(MDCUtil.getCurrentRequestId())
                .timestamp(OffsetDateTime.now())
                .build();
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("Failed to send error response", e);
        }
    }
}
