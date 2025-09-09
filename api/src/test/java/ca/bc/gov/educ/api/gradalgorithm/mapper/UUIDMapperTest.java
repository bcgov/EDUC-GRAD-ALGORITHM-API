package ca.bc.gov.educ.api.gradalgorithm.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UUIDMapperTest {

    private UUIDMapper uuidMapper;

    @Before
    public void setUp() {
        uuidMapper = new UUIDMapper();
    }

    @Test
    public void testMapString_WithValidUUID_ShouldReturnUUID() {
        // Given
        String validUUIDString = "123e4567-e89b-12d3-a456-426614174000";

        // When
        UUID result = uuidMapper.map(validUUIDString);

        // Then
        assertNotNull(result);
        assertEquals(validUUIDString, result.toString());
    }

    @Test
    public void testMapString_WithNullString_ShouldReturnNull() {
        // Given
        String nullString = null;

        // When
        UUID result = uuidMapper.map(nullString);

        // Then
        assertNull(result);
    }

    @Test
    public void testMapString_WithEmptyString_ShouldReturnNull() {
        // Given
        String emptyString = "";

        // When
        UUID result = uuidMapper.map(emptyString);

        // Then
        assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapString_WithInvalidUUID_ShouldThrowException() {
        // Given
        String invalidUUIDString = "invalid-uuid-string";

        // When
        uuidMapper.map(invalidUUIDString);

        // Then - exception should be thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapString_WithMalformedUUID_ShouldThrowException() {
        // Given
        String malformedUUIDString = "123e4567-e89b-12d3-a456";

        // When
        uuidMapper.map(malformedUUIDString);

        // Then - exception should be thrown
    }

    @Test
    public void testMapUUID_WithValidUUID_ShouldReturnString() {
        // Given
        UUID validUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // When
        String result = uuidMapper.map(validUUID);

        // Then
        assertNotNull(result);
        assertEquals("123e4567-e89b-12d3-a456-426614174000", result);
    }

    @Test
    public void testMapUUID_WithNullUUID_ShouldReturnNull() {
        // Given
        UUID nullUUID = null;

        // When
        String result = uuidMapper.map(nullUUID);

        // Then
        assertNull(result);
    }
}