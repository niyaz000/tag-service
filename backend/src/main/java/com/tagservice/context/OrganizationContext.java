package com.tagservice.context;

import org.slf4j.MDC;

/**
 * Context to hold the current organization ID.
 */
public class OrganizationContext {

    private static final ThreadLocal<String> currentOrganization = new ThreadLocal<>();

    private static final String MDC_ORGANIZATION_ID_KEY = "organizationId";

    public static void setOrganizationId(String organizationId) {
        currentOrganization.set(organizationId);
        MDC.put(MDC_ORGANIZATION_ID_KEY, organizationId);
    }

    public static String getOrganizationId() {
        return currentOrganization.get();
    }

    public static void clear() {
        currentOrganization.remove();
        MDC.remove(MDC_ORGANIZATION_ID_KEY);
    }
}
