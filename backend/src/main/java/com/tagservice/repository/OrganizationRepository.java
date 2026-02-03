package com.tagservice.repository;

import com.tagservice.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Organization entities.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Checks if an active (non-soft-deleted) organization exists by ID.
     */
    boolean existsByIdAndDeletedAtIsNull(Long id);

    /**
     * Finds an active (non-soft-deleted) organization by ID.
     */
    Optional<Organization> findByIdAndDeletedAtIsNull(Long id);
}

