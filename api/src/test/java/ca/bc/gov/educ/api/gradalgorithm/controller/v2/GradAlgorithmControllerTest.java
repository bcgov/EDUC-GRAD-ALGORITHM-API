package ca.bc.gov.educ.api.gradalgorithm.controller.v2;

import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.service.GradAlgorithmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GradAlgorithmControllerTest {

    @Mock
    private GradAlgorithmService gradAlgorithmService;

    @InjectMocks
    private GradAlgorithmController gradAlgorithmController;

    private static final String STUDENT_ID = "12345678-1234-1234-1234-123456789012";
    private static final String GRAD_PROGRAM = "2018-EN";

    @Test
    public void testGraduateStudent_WithValidParameters_ShouldReturnGraduationData() {
        // Arrange
        GraduationData expectedGraduationData = new GraduationData();
        expectedGraduationData.setGraduated(true);
        
        when(gradAlgorithmService.graduateStudent(
            UUID.fromString(STUDENT_ID),
            eq(GRAD_PROGRAM), 
            eq(false), 
            eq(""), 
            eq(true)
        )).thenReturn(expectedGraduationData);

        // Act
        GraduationData result = gradAlgorithmController.graduateStudent(
            STUDENT_ID, 
            GRAD_PROGRAM, 
            false, 
            ""
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.isGraduated());
        verify(gradAlgorithmService, times(1)).graduateStudent(
            UUID.fromString(STUDENT_ID), 
            GRAD_PROGRAM, 
            false, 
            "", 
            true
        );
    }

    @Test
    public void testGraduateStudent_WithProjectedTrue_ShouldCallServiceWithProjectedTrue() {
        // Arrange
        GraduationData expectedGraduationData = new GraduationData();
        expectedGraduationData.setGraduated(false);
        
        when(gradAlgorithmService.graduateStudent(
            UUID.fromString(STUDENT_ID),
            eq(GRAD_PROGRAM), 
            eq(true), 
            eq("2024"), 
            eq(true)
        )).thenReturn(expectedGraduationData);

        // Act
        GraduationData result = gradAlgorithmController.graduateStudent(
            STUDENT_ID, 
            GRAD_PROGRAM, 
            true, 
            "2024"
        );

        // Assert
        assertNotNull(result);
        assertFalse(result.isGraduated());
        verify(gradAlgorithmService, times(1)).graduateStudent(
            UUID.fromString(STUDENT_ID), 
            GRAD_PROGRAM, 
            true, 
            "2024", 
            true
        );
    }

    @Test
    public void testGraduateStudent_WithInvalidStudentId_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidStudentId = "invalid-uuid";

        // Act & Assert
        try {
            gradAlgorithmController.graduateStudent(
                invalidStudentId, 
                GRAD_PROGRAM, 
                false, 
                ""
            );
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid UUID string"));
        }
        
        verify(gradAlgorithmService, never()).graduateStudent(any(), any(), anyBoolean(), any(), anyBoolean());
    }
}
