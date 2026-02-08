package com.tagservice.enums;

/**
 * Enumeration of standard API error types used in the service.
 * <p>
 * Each type maps to a RFC 7807-compatible URI and a human-readable title.
 */
public enum ApiErrorType {

    VALIDATION_ERROR("validation-error", "Validation Error"),
    DUPLICATE_ENTITY("duplicate-entity", "Duplicate Entity"),

    MISSING_HEADER("missing-header", "Missing Required Header"),
    INVALID_HEADER("invalid-header", "Invalid Header Format"),

    MISSING_ORGANIZATION("missing-organization", "Organization Context Missing"),
    INVALID_ORGANIZATION_ID("invalid-organization-id", "Invalid Organization Identifier"),
    ORGANIZATION_DELETED("organization-deleted", "Organization Deleted"),
    ORGANIZATION_NOT_FOUND("organization-not-found", "Organization Not Found");

    private static final String BASE_URI = "https://api.tag-service.com/errors#";

    private final String code;
    private final String title;

    ApiErrorType(String code, String title) {
        this.code = code;
        this.title = title;
    }

    /**
     * Returns the full URI for this error type, e.g.
     * {@code https://api.tag-service.com/errors#validation-error}.
     */
    public String getTypeUri() {
        return BASE_URI + code;
    }

    /**
     * Human-readable title corresponding to this error type.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Short code for this error type (suffix of the URI).
     */
    public String getCode() {
        return code;
    }
}

