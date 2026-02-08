package com.tagservice.service;

import com.tagservice.model.Organization;
import com.tagservice.repository.OrganizationRepository;
import com.tagservice.request.OrganizationCreateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for organization-related operations.
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    /**
     * Creates a new organization from the incoming request payload.
     *
     * @param request the create organization request
     * @return the persisted organization entity
     */
    @Transactional
    public Organization createOrganization(OrganizationCreateRequest request) {
        Organization organization = Organization.builder()
                .name(request.getName())
                .displayName(request.getDisplayName())
                .domain(request.getDomain())
                .type(request.getType())
                .build();

        return organizationRepository.save(organization);
    }

    /**
     * Fetches an active (non-soft-deleted) organization by ID.
     *
     * @param id the organization ID
     * @return the organization entity
     */
    @Transactional(readOnly = true)
    public Organization getOrganizationById(Long id) {
        return organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));
    }
}


