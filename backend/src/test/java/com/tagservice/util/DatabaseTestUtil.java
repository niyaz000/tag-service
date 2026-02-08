package com.tagservice.util;

import com.tagservice.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for database operations in tests.
 * Provides methods to clean up test data and ensure database isolation.
 */
@Component
public class DatabaseTestUtil {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public DatabaseTestUtil(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Cleans up all organizations from the database.
     * This ensures a clean state for each test.
     */
    public void cleanupOrganizations() {
        organizationRepository.deleteAll();
    }

    /**
     * Cleans up all test data from the database.
     * This is a general cleanup method that can be extended for other entities.
     */
    public void cleanupAll() {
        cleanupOrganizations();
        // Add cleanup for other entities as needed
    }
}
