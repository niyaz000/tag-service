package com.tagservice.controller;

import com.tagservice.client.OrganizationClient;
import com.tagservice.request.OrganizationCreateRequest;
import com.tagservice.response.OrganizationCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for organization management.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Validated
public class OrganizationController {

    private final OrganizationClient organizationClient;
    @PostMapping("/organizations")
    public ResponseEntity<OrganizationCreateResponse> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request) {
        OrganizationCreateResponse response = organizationClient.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
