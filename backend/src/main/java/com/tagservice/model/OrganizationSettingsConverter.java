package com.tagservice.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter to map {@link OrganizationSettings} to a JSON string column (jsonb)
 * and back.
 */
@Converter
public class OrganizationSettingsConverter implements AttributeConverter<OrganizationSettings, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(OrganizationSettings attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert OrganizationSettings to JSON", e);
        }
    }

    @Override
    public OrganizationSettings convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, OrganizationSettings.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert JSON to OrganizationSettings", e);
        }
    }
}

