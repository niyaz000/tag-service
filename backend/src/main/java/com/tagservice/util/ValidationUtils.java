package com.tagservice.util;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Utility class for common validation operations.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates if the given string is a valid UUID format.
     * 
     * @param uuid the string to validate
     * @return true if valid UUID, false otherwise
     */
    public static boolean isValidUUID(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return false;
        }

        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
