package com.tagservice.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard API error response following the project's error format.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String request_id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime timestamp;

    private List<ApiSubError> errors;

    @Data
    @Builder
    public static class ApiSubError {
        private String field;
        private String type;
        private String description;
    }
}
