package com.tagservice.util;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test execution listener for integration tests.
 * Handles database cleanup and MockMvc setup automatically.
 */
public class IntegrationTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        // Clean up database before each test to ensure clean state
        cleanupDatabase(testContext);
        
        // Setup MockMvc for REST Assured if MockMvc is available
        setupMockMvc(testContext);
    }

    private void setupMockMvc(TestContext testContext) {
        if (testContext.getApplicationContext() == null) {
            throw new RuntimeException("Application context is not available for MockMvc setup");
        }
        
        try {
            MockMvc mockMvc = testContext.getApplicationContext().getBean(MockMvc.class);
            if (mockMvc != null) {
                RestAssuredMockMvc.mockMvc(mockMvc);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup MockMvc for REST Assured", e);
        }
    }

    private void cleanupDatabase(TestContext testContext) {
        if (testContext.getApplicationContext() == null) {
            throw new RuntimeException("Application context is not available for database cleanup");
        }
        
        try {
            DatabaseTestUtil databaseTestUtil = testContext.getApplicationContext().getBean(DatabaseTestUtil.class);
            if (databaseTestUtil != null) {
                databaseTestUtil.cleanupAll();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to cleanup database", e);
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        // Clean up database after each test to ensure isolation
        cleanupDatabase(testContext);
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        // Clean up database before test class starts to ensure clean state
        cleanupDatabase(testContext);
    }
}
