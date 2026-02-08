package com.tagservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response payload returned after creating a new Organization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreateResponse {

    private Long id;
    private String name;
    private String displayName;
    private String domain;
    private String type;
    private String settings;
    private OffsetDateTime deletedAt;
}

