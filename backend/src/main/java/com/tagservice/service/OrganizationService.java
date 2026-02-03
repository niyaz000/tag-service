package com.tagservice.service;

import com.tagservice.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for organization-related operations.
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    /**
     * Checks whether an organization exists with the given ID.
     * Soft-deleted organizations (with non-null deleted_at) are treated as non-existent.
     *
     * @param id the organization ID
     * @return the organization details
     */
    public OrganizationDto getOrganizationById(Long id) {
        var organization = organizationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));
        return OrganizationDto.builder()
                .id(organization.getId())
                .deletedAt(organization.getDeletedAt())
                .build();
    }
}


