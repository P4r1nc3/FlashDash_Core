package com.flashdash.converter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonListConverterTest {

    private final JsonListConverter converter = new JsonListConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        // Given
        List<String> inputList = Arrays.asList("Answer1", "Answer2", "Answer3");

        // When
        String result = converter.convertToDatabaseColumn(inputList);

        // Then
        assertNotNull(result);
        assertEquals("[\"Answer1\",\"Answer2\",\"Answer3\"]", result);
    }

    @Test
    public void testConvertToEntityAttribute() {
        // Given
        String json = "[\"Answer1\",\"Answer2\",\"Answer3\"]";

        // When
        List<String> result = converter.convertToEntityAttribute(json);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Answer1", result.get(0));
        assertEquals("Answer2", result.get(1));
        assertEquals("Answer3", result.get(2));
    }

    @Test
    public void testConvertToDatabaseColumnWithEmptyList() {
        // Given
        List<String> inputList = Arrays.asList();

        // When
        String result = converter.convertToDatabaseColumn(inputList);

        // Then
        assertNotNull(result);
        assertEquals("[]", result);
    }

    @Test
    public void testConvertToEntityAttributeWithEmptyJson() {
        // Given
        String json = "[]";

        // When
        List<String> result = converter.convertToEntityAttribute(json);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testConvertToEntityAttributeWithNull() {
        // Given
        String json = null;

        // When
        List<String> result = converter.convertToEntityAttribute(json);

        // Then
        assertNull(result);
    }

    @Test
    public void testConvertToDatabaseColumnWithNull() {
        // Given
        List<String> inputList = null;

        // When
        String result = converter.convertToDatabaseColumn(inputList);

        // Then
        assertNull(result);
    }

    @Test
    public void testConvertToEntityAttributeWithMalformedJson() {
        // Given
        String malformedJson = "[\"Answer1\",\"Answer2\"";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(malformedJson));
    }
}
