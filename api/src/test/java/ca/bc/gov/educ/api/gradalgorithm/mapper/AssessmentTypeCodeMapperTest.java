package ca.bc.gov.educ.api.gradalgorithm.mapper;

import ca.bc.gov.educ.api.gradalgorithm.dto.Assessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentTypeCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AssessmentTypeCodeMapperTest {

    final static private AssessmentTypeCodeMapper mapper = AssessmentTypeCodeMapper.mapper;

    private AssessmentTypeCode testAssessmentTypeCode;

    @Before
    public void setUp() {
        testAssessmentTypeCode = AssessmentTypeCode.builder()
                .assessmentTypeCode("MATH10")
                .label("Mathematics 10")
                .displayOrder(1)
                .language("EN")
                .effectiveDate("2023-01-01T00:00:00")
                .expiryDate("2024-12-31T23:59:59")
                .build();
    }

    @Test
    public void testToAssessment_WithValidData_ShouldMapCorrectly() {
        // Given
        Date expectedStartDate = Date.valueOf(LocalDate.of(2023, 1, 1));
        Date expectedEndDate = Date.valueOf(LocalDate.of(2024, 12, 31));

        // When
        Assessment result = mapper.toAssessment(testAssessmentTypeCode);

        // Then
        assertNotNull(result);
        assertEquals("MATH10", result.getAssessmentCode());
        assertEquals("Mathematics 10", result.getAssessmentName());
        assertEquals("EN", result.getLanguage());
        assertEquals(expectedStartDate, result.getStartDate());
        assertEquals(expectedEndDate, result.getEndDate());
    }

    @Test
    public void testToAssessment_WithNullValues_ShouldMapCorrectly() {
        // Given
        testAssessmentTypeCode = AssessmentTypeCode.builder()
                .assessmentTypeCode(null)
                .label(null)
                .language(null)
                .effectiveDate(null)
                .expiryDate(null)
                .build();

        // When
        Assessment result = mapper.toAssessment(testAssessmentTypeCode);

        // Then
        assertNotNull(result);
        assertNull(result.getAssessmentCode());
        assertNull(result.getAssessmentName());
        assertNull(result.getLanguage());
        assertNull(result.getStartDate());
        assertNull(result.getEndDate());
    }

    @Test
    public void testToAssessmentList_WithValidList_ShouldMapCorrectly() {
        // Given
        AssessmentTypeCode assessment1 = AssessmentTypeCode.builder()
                .assessmentTypeCode("MATH10")
                .label("Mathematics 10")
                .language("EN")
                .build();

        AssessmentTypeCode assessment2 = AssessmentTypeCode.builder()
                .assessmentTypeCode("SCI10")
                .label("Science 10")
                .language("EN")
                .build();

        List<AssessmentTypeCode> assessmentTypeCodes = Arrays.asList(assessment1, assessment2);

        // When
        List<Assessment> result = mapper.toAssessmentList(assessmentTypeCodes);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MATH10", result.get(0).getAssessmentCode());
        assertEquals("SCI10", result.get(1).getAssessmentCode());
    }

    @Test
    public void testToAssessmentList_WithNullList_ShouldReturnNull() {
        // Given
        List<AssessmentTypeCode> nullList = null;

        // When
        List<Assessment> result = mapper.toAssessmentList(nullList);

        // Then
        assertNull(result);
    }
}
