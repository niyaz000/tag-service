package com.tagservice.controller;

import com.tagservice.request.OrganizationCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for OrganizationController using REST Assured MockMvc with real implementation.
 * Database cleanup and MockMvc setup are handled automatically by IntegrationTestExecutionListener.
 */
@IntegrationTest
class OrganizationControllerTest {

    @Nested
    @DisplayName("Create Organization")
    class Create {

        @Nested
        @DisplayName("Valid Cases")
        class Valid {

            @Test
            @DisplayName("Should create organization successfully with all required fields")
            void given_validRequestWithAllFields_when_createOrganization_then_returns201() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("Test Organization")
                        .domain("test.com")
                        .type("enterprise")
                        .settings("{\"features\":[\"feature1\"]}")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("id", greaterThan(0))
                        .body("name", equalTo("TestOrg"))
                        .body("displayName", equalTo("Test Organization"))
                        .body("domain", equalTo("test.com"))
                        .body("type", equalTo("enterprise"))
                        .body("settings", equalTo("{\"features\":[\"feature1\"]}"))
                        .body("deletedAt", nullValue());
            }

            @Test
            @DisplayName("Should create organization successfully with only required fields")
            void given_validRequestWithRequiredFieldsOnly_when_createOrganization_then_returns201() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("MinimalOrg")
                        .displayName("Minimal Organization")
                        .domain("minimal.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("id", greaterThan(0))
                        .body("name", equalTo("MinimalOrg"))
                        .body("displayName", equalTo("Minimal Organization"))
                        .body("domain", equalTo("minimal.com"))
                        .body("type", nullValue())
                        .body("settings", nullValue());
            }

            @Test
            @DisplayName("Should handle long field values")
            void given_requestWithLongFieldValues_when_createOrganization_then_returns201() throws Exception {
                // Given
                String longName = "A".repeat(100);
                String longDisplayName = "B".repeat(255);
                String longDomain = "C".repeat(255);

                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name(longName)
                        .displayName(longDisplayName)
                        .domain(longDomain)
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("id", greaterThan(0))
                        .body("name", equalTo(longName))
                        .body("displayName", equalTo(longDisplayName))
                        .body("domain", equalTo(longDomain));
            }

            @Test
            @DisplayName("Should create organization with special characters in fields")
            void given_requestWithSpecialCharacters_when_createOrganization_then_returns201() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("Org-123_Test")
                        .displayName("Test & Organization (Ltd.)")
                        .domain("test-domain.co.uk")
                        .type("enterprise-v2")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("id", greaterThan(0))
                        .body("name", equalTo("Org-123_Test"))
                        .body("displayName", equalTo("Test & Organization (Ltd.)"))
                        .body("domain", equalTo("test-domain.co.uk"))
                        .body("type", equalTo("enterprise-v2"));
            }
        }

        @Nested
        @DisplayName("Invalid Cases")
        class Invalid {

            @Test
            @DisplayName("Should return 400 when name is missing")
            void given_requestWithMissingName_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name(null)
                        .displayName("Test Organization")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when name is blank")
            void given_requestWithBlankName_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("   ")
                        .displayName("Test Organization")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when name is empty")
            void given_requestWithEmptyName_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("")
                        .displayName("Test Organization")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when displayName is missing")
            void given_requestWithMissingDisplayName_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName(null)
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when displayName is blank")
            void given_requestWithBlankDisplayName_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("   ")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when displayName is empty")
            void given_requestWithEmptyDisplayName_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when domain is missing")
            void given_requestWithMissingDomain_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("Test Organization")
                        .domain(null)
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when domain is blank")
            void given_requestWithBlankDomain_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("Test Organization")
                        .domain("   ")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when domain is empty")
            void given_requestWithEmptyDomain_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("Test Organization")
                        .domain("")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when all required fields are missing")
            void given_requestWithAllRequiredFieldsMissing_when_createOrganization_then_returns400() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name(null)
                        .displayName(null)
                        .domain(null)
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when request body is missing")
            void given_missingRequestBody_when_createOrganization_then_returns400() throws Exception {
                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when request body is invalid JSON")
            void given_invalidJson_when_createOrganization_then_returns400() throws Exception {
                // When & Then
                given()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body("{ invalid json }")
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when Content-Type is missing")
            void given_missingContentType_when_createOrganization_then_returns415() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("Test Organization")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(415); // Unsupported Media Type
            }

            @Test
            @DisplayName("Should return 400 when Content-Type is not application/json")
            void given_invalidContentType_when_createOrganization_then_returns415() throws Exception {
                // Given
                OrganizationCreateRequest request = OrganizationCreateRequest.builder()
                        .name("TestOrg")
                        .displayName("Test Organization")
                        .domain("test.com")
                        .build();

                // When & Then
                given()
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .body(request)
                        .when()
                        .post("/v1/organizations")
                        .then()
                        .statusCode(415); // Unsupported Media Type
            }
        }
    }
}
