package com.ttn.redish.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class HeavyPayloadJsonConverter implements AttributeConverter<HeavyPayload, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(HeavyPayload attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize HeavyPayload", e);
        }
    }

    @Override
    public HeavyPayload convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, HeavyPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize HeavyPayload", e);
        }
    }
}
