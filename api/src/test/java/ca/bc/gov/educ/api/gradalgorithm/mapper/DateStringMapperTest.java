package ca.bc.gov.educ.api.gradalgorithm.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DateStringMapperTest {

    private DateStringMapper dateStringMapper;

    @Before
    public void setUp() {
        dateStringMapper = new DateStringMapper();
    }

    @Test
    public void testMap_WithValidISODateTime_ShouldReturnCorrectDate() {
        // Given
        String validDateTimeString = "2023-01-15T10:30:00";

        // When
        Date result = dateStringMapper.map(validDateTimeString);

        // Then
        assertNotNull(result);
        assertEquals(Date.valueOf(LocalDate.of(2023, 1, 15)), result);
    }

    @Test
    public void testMap_WithNullString_ShouldReturnNull() {
        // Given
        String nullString = null;

        // When
        Date result = dateStringMapper.map(nullString);

        // Then
        assertNull(result);
    }

    @Test
    public void testMap_WithEmptyString_ShouldReturnNull() {
        // Given
        String emptyString = "";

        // When
        Date result = dateStringMapper.map(emptyString);

        // Then
        assertNull(result);
    }

    @Test(expected = Exception.class)
    public void testMap_WithInvalidDateFormat_ShouldThrowException() {
        // Given
        String invalidDateString = "2023-13-45T25:70:80";

        // When
        dateStringMapper.map(invalidDateString);

        // Then - exception should be thrown
    }

    @Test(expected = Exception.class)
    public void testMap_WithInvalidString_ShouldThrowException() {
        // Given
        String invalidString = "not-a-date";

        // When
        dateStringMapper.map(invalidString);

        // Then - exception should be thrown
    }

    @Test
    public void testMap_WithLeapYearDate_ShouldReturnCorrectDate() {
        // Given
        String leapYearDateTimeString = "2024-02-29T12:00:00";

        // When
        Date result = dateStringMapper.map(leapYearDateTimeString);

        // Then
        assertNotNull(result);
        assertEquals(Date.valueOf(LocalDate.of(2024, 2, 29)), result);
    }
}
