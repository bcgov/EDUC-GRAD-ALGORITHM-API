package ca.bc.gov.educ.api.gradalgorithm.mapper;

import ca.bc.gov.educ.api.gradalgorithm.constants.ProvincialSpecialCaseCodes;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentStudentListItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssessmentStudentAlgorithmMapperTest {

    @Mock
    private AssessmentStudentAlgorithmMapper delegate;

    private TestAssessmentStudentAlgorithmMapperDecorator decorator;

    private AssessmentStudentListItem testAssessmentStudent;

    @Before
    public void setUp() {
        decorator = new TestAssessmentStudentAlgorithmMapperDecorator(delegate);
        testAssessmentStudent = AssessmentStudentListItem.builder()
                .pen("123456789")
                .proficiencyScore("85.5")
                .irtScore("75.2")
                .provincialSpecialCaseCode(null)
                .numberOfAttempts("2")
                .assessmentTypeCode("MATH10")
                .courseMonth("06")
                .courseYear("2023")
                .build();
    }

    @Test
    public void testToAlgorithmData_WithValidData_ShouldMapCorrectly() {
        // Given
        StudentAssessment expectedResult = new StudentAssessment();
        expectedResult.setPen("123456789");
        expectedResult.setAssessmentCode("MATH10");
        expectedResult.setSpecialCase(null);
        expectedResult.setProficiencyScore(85.5);
        expectedResult.setRawScore(75.2);

        when(delegate.toAlgorithmData(testAssessmentStudent)).thenReturn(expectedResult);

        // When
        StudentAssessment result = decorator.toAlgorithmData(testAssessmentStudent);

        // Then
        assertNotNull(result);
        assertEquals("123456789", result.getPen());
        assertEquals("MATH10", result.getAssessmentCode());
        assertEquals(Double.valueOf(85.5), result.getProficiencyScore());
        assertEquals(Double.valueOf(75.2), result.getRawScore());
        assertEquals("Y", result.getWroteFlag());
        assertEquals("N", result.getExceededWriteFlag());
        assertEquals("2023/06", result.getSessionDate());
        verify(delegate).toAlgorithmData(testAssessmentStudent);
    }

    @Test
    public void testToAlgorithmData_WithSpecialCase_ShouldSetWroteFlagToY() {
        // Given
        testAssessmentStudent.setProvincialSpecialCaseCode(ProvincialSpecialCaseCodes.NOTCOMPLETED.getCode());
        testAssessmentStudent.setProficiencyScore(null);

        StudentAssessment expectedResult = new StudentAssessment();
        expectedResult.setPen("123456789");
        expectedResult.setAssessmentCode("MATH10");
        expectedResult.setSpecialCase(ProvincialSpecialCaseCodes.NOTCOMPLETED.getCode());

        when(delegate.toAlgorithmData(testAssessmentStudent)).thenReturn(expectedResult);

        // When
        StudentAssessment result = decorator.toAlgorithmData(testAssessmentStudent);

        // Then
        assertNotNull(result);
        assertEquals("Y", result.getWroteFlag());
        assertEquals(ProvincialSpecialCaseCodes.NOTCOMPLETED.getCode(), result.getSpecialCase());
    }

    @Test
    public void testToAlgorithmData_WithNoProficiencyScoreAndNoSpecialCase_ShouldSetWroteFlagToN() {
        // Given
        testAssessmentStudent.setProficiencyScore(null);
        testAssessmentStudent.setProvincialSpecialCaseCode(null);

        StudentAssessment expectedResult = new StudentAssessment();
        expectedResult.setPen("123456789");
        expectedResult.setAssessmentCode("MATH10");
        expectedResult.setSpecialCase(null);

        when(delegate.toAlgorithmData(testAssessmentStudent)).thenReturn(expectedResult);

        // When
        StudentAssessment result = decorator.toAlgorithmData(testAssessmentStudent);

        // Then
        assertNotNull(result);
        assertEquals("N", result.getWroteFlag());
    }

    @Test
    public void testToAlgorithmData_WithNumberOfAttemptsGreaterThanOrEqualTo3_ShouldSetExceededWriteFlagToY() {
        // Given
        testAssessmentStudent.setNumberOfAttempts("3");

        StudentAssessment expectedResult = new StudentAssessment();
        expectedResult.setPen("123456789");
        expectedResult.setAssessmentCode("MATH10");

        when(delegate.toAlgorithmData(testAssessmentStudent)).thenReturn(expectedResult);

        // When
        StudentAssessment result = decorator.toAlgorithmData(testAssessmentStudent);

        // Then
        assertNotNull(result);
        assertEquals("Y", result.getExceededWriteFlag());
    }

    @Test
    public void testToAlgorithmData_WithNullNumberOfAttempts_ShouldSetExceededWriteFlagToN() {
        // Given
        testAssessmentStudent.setNumberOfAttempts(null);

        StudentAssessment expectedResult = new StudentAssessment();
        expectedResult.setPen("123456789");
        expectedResult.setAssessmentCode("MATH10");

        when(delegate.toAlgorithmData(testAssessmentStudent)).thenReturn(expectedResult);

        // When
        StudentAssessment result = decorator.toAlgorithmData(testAssessmentStudent);

        // Then
        assertNotNull(result);
        assertEquals("N", result.getExceededWriteFlag());
    }

    // Concrete implementation of the abstract decorator for testing
    private static class TestAssessmentStudentAlgorithmMapperDecorator extends AssessmentStudentAlgorithmMapperDecorator {
        public TestAssessmentStudentAlgorithmMapperDecorator(AssessmentStudentAlgorithmMapper delegate) {
            super(delegate);
        }
    }
}
