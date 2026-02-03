package com.tagservice.util;

import com.tagservice.context.OrganizationContext;
import org.slf4j.MDC;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) with automatic cleanup.
 */
public final class MDCUtil {

    private static final String MDC_ORGANIZATION_ID_KEY = "organizationId";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    private MDCUtil() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Executes a Runnable with the specified MDC key-value pair set.
     * The MDC key is automatically removed in a finally block to prevent memory leaks.
     *
     * @param runnable the code to execute
     * @param key      the MDC key to set
     * @param value    the MDC value to set
     */
    public static void runWithMdc(Runnable runnable, String key, String value) {
        try {
            MDC.put(key, value);
            runnable.run();
        } finally {
            MDC.remove(key);
        }
    }

    /**
     * Executes a Runnable with the organization ID set in both OrganizationContext and MDC.
     * Ensures both contexts are cleared in a finally block.
     *
     * @param runnable       the code to execute
     * @param organizationId the organization ID to set
     */
    public static void runWithOrganizationId(Runnable runnable, String organizationId) {
        try {
            OrganizationContext.setOrganizationId(organizationId);
            MDC.put(MDC_ORGANIZATION_ID_KEY, organizationId);
            runnable.run();
        } finally {
            OrganizationContext.clear();
            MDC.remove(MDC_ORGANIZATION_ID_KEY);
        }
    }

    /**
     * Returns the current request ID from MDC, if present.
     *
     * @return the request ID or null if not set
     */
    public static String getCurrentRequestId() {
        return MDC.get(MDC_REQUEST_ID_KEY);
    }

    public static UUID getCurrentRequestIdAsUUID() {
        return UUID.fromString(getCurrentRequestId());
    }
}

