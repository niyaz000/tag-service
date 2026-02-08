package com.tagservice.util;

import jakarta.servlet.ServletException;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) values.
 * Provides methods to set and retrieve request IDs and organization IDs from MDC.
 */
public class MDCUtil {

    private static final String MDC_REQUEST_ID_KEY = "requestId";
    private static final String MDC_ORGANIZATION_ID_KEY = "organizationId";

    /**
     * Gets the current request ID from MDC.
     *
     * @return the request ID, or null if not set
     */
    public static String getCurrentRequestId() {
        return MDC.get(MDC_REQUEST_ID_KEY);
    }

    /**
     * Gets the current request ID from MDC as a UUID.
     *
     * @return the request ID as UUID, or null if not set or invalid
     */
    public static UUID getCurrentRequestIdAsUUID() {
        String requestId = getCurrentRequestId();
        if (requestId != null) {
            try {
                return UUID.fromString(requestId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Executes a runnable with the specified request ID set in MDC.
     * The request ID is automatically cleared after execution.
     *
     * @param runnable  the code to execute
     * @param requestId the request ID to set in MDC
     */
    public static void runWithRequestId(Runnable runnable, String requestId) {
        String previousRequestId = MDC.get(MDC_REQUEST_ID_KEY);
        try {
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
            runnable.run();
        } finally {
            if (previousRequestId != null) {
                MDC.put(MDC_REQUEST_ID_KEY, previousRequestId);
            } else {
                MDC.remove(MDC_REQUEST_ID_KEY);
            }
        }
    }

    /**
     * Executes a filter runnable with the specified request ID set in MDC.
     * The request ID is automatically cleared after execution.
     *
     * @param runnable  the code to execute
     * @param requestId the request ID to set in MDC
     * @throws IOException if the runnable throws IOException
     * @throws ServletException if the runnable throws ServletException
     */
    public static void runWithRequestId(FilterRunnable runnable, String requestId) 
            throws IOException, ServletException {
        String previousRequestId = MDC.get(MDC_REQUEST_ID_KEY);
        try {
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
            runnable.run();
        } finally {
            if (previousRequestId != null) {
                MDC.put(MDC_REQUEST_ID_KEY, previousRequestId);
            } else {
                MDC.remove(MDC_REQUEST_ID_KEY);
            }
        }
    }

    /**
     * Executes a runnable with the specified organization ID set in MDC.
     * The organization ID is automatically cleared after execution.
     *
     * @param runnable      the code to execute
     * @param organizationId the organization ID to set in MDC
     */
    public static void runWithOrganizationId(Runnable runnable, String organizationId) {
        String previousOrganizationId = MDC.get(MDC_ORGANIZATION_ID_KEY);
        try {
            MDC.put(MDC_ORGANIZATION_ID_KEY, organizationId);
            runnable.run();
        } finally {
            if (previousOrganizationId != null) {
                MDC.put(MDC_ORGANIZATION_ID_KEY, previousOrganizationId);
            } else {
                MDC.remove(MDC_ORGANIZATION_ID_KEY);
            }
        }
    }

    /**
     * Functional interface for code that may throw IOException or ServletException.
     */
    @FunctionalInterface
    public interface FilterRunnable {
        void run() throws IOException, ServletException;
    }

    /**
     * Executes a filter runnable with the specified organization ID set in MDC.
     * The organization ID is automatically cleared after execution.
     *
     * @param runnable      the code to execute
     * @param organizationId the organization ID to set in MDC
     * @throws IOException if the runnable throws IOException
     * @throws ServletException if the runnable throws ServletException
     */
    public static void runWithOrganizationId(FilterRunnable runnable, String organizationId) 
            throws IOException, ServletException {
        String previousOrganizationId = MDC.get(MDC_ORGANIZATION_ID_KEY);
        try {
            MDC.put(MDC_ORGANIZATION_ID_KEY, organizationId);
            runnable.run();
        } finally {
            if (previousOrganizationId != null) {
                MDC.put(MDC_ORGANIZATION_ID_KEY, previousOrganizationId);
            } else {
                MDC.remove(MDC_ORGANIZATION_ID_KEY);
            }
        }
    }
}
