package com.tagservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the JSONB settings for an Organization.
 * <p>
 * This structure is aligned with the public API specification and can be
 * extended as the spec evolves.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSettings {

    /**
     * Configuration constraints that apply to the organization.
     */
    private List<String> constraints;

    /**
     * Enabled features for the organization.
     */
    private List<String> features;
}

