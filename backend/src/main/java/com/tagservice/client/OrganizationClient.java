package com.tagservice.client;

import com.tagservice.dto.error.OrganizationDto;
import com.tagservice.model.Organization;
import com.tagservice.request.OrganizationCreateRequest;
import com.tagservice.response.OrganizationCreateResponse;
import com.tagservice.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Client for organization-related operations.
 * <p>
 * This class acts as a facade over the service layer, providing
 * request/response mapping for controller and other callers.
 */
@Service
@RequiredArgsConstructor
public class OrganizationClient {
   
    
    private final OrganizationService organizationService;

    public OrganizationDto getActiveOrganizationById(Long id) {
        var org = organizationService.getOrganizationById(id);
        return OrganizationDto.builder()
                .id(org.getId())
                .deletedAt(org.getDeletedAt())
                .build();
    }

    /**
     * Creates a new organization and returns the API response DTO.
     */
    public OrganizationCreateResponse createOrganization(OrganizationCreateRequest request) {
        Organization organization = organizationService.createOrganization(request);
        return OrganizationCreateResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .displayName(organization.getDisplayName())
                .domain(organization.getDomain())
                .type(organization.getType())
                .settings(request.getSettings())
                .deletedAt(organization.getDeletedAt())
                .build();
    }
}

  