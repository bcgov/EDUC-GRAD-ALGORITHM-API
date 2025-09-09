package ca.bc.gov.educ.api.gradalgorithm.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalDateTimeMapperTest {

    private LocalDateTimeMapper localDateTimeMapper;

    @Before
    public void setUp() {
        localDateTimeMapper = new LocalDateTimeMapper();
    }

    @Test
    public void testMapLocalDateTimeToString_WithValidLocalDateTime_ShouldReturnCorrectString() {
        // Given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 15, 10, 30, 45);

        // When
        String result = localDateTimeMapper.map(localDateTime);

        // Then
        assertNotNull(result);
        assertEquals("2023-01-15T10:30:45", result);
    }

    @Test
    public void testMapLocalDateTimeToString_WithNullLocalDateTime_ShouldReturnNull() {
        // Given
        LocalDateTime nullLocalDateTime = null;

        // When
        String result = localDateTimeMapper.map(nullLocalDateTime);

        // Then
        assertNull(result);
    }

    @Test
    public void testMapStringToLocalDateTime_WithValidString_ShouldReturnCorrectLocalDateTime() {
        // Given
        String validDateTimeString = "2023-01-15T10:30:45";

        // When
        LocalDateTime result = localDateTimeMapper.map(validDateTimeString);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2023, 1, 15, 10, 30, 45), result);
    }

    @Test
    public void testMapStringToLocalDateTime_WithNullString_ShouldReturnNull() {
        // Given
        String nullString = null;

        // When
        LocalDateTime result = localDateTimeMapper.map(nullString);

        // Then
        assertNull(result);
    }

    @Test(expected = Exception.class)
    public void testMapStringToLocalDateTime_WithInvalidString_ShouldThrowException() {
        // Given
        String invalidString = "not-a-datetime";

        // When
        localDateTimeMapper.map(invalidString);

        // Then - exception should be thrown
    }

    @Test
    public void testRoundTripMapping_ShouldPreserveValue() {
        // Given
        LocalDateTime originalLocalDateTime = LocalDateTime.of(2023, 1, 15, 10, 30, 45);

        // When
        String dateTimeString = localDateTimeMapper.map(originalLocalDateTime);
        LocalDateTime result = localDateTimeMapper.map(dateTimeString);

        // Then
        assertNotNull(dateTimeString);
        assertNotNull(result);
        assertEquals(originalLocalDateTime, result);
    }
}
