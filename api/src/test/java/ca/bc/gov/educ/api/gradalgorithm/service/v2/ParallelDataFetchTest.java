package ca.bc.gov.educ.api.gradalgorithm.service.v2;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ParallelDataFetchTest {

    @Mock
    private GradCourseService gradCourseService;

    @Mock
    private GradAssessmentService gradAssessmentService;

    @InjectMocks
    private ParallelDataFetch parallelDataFetch;

    private UUID testStudentID;
    private ExceptionMessage testException;

    @Before
    public void setUp() {
        testStudentID = UUID.randomUUID();
        testException = new ExceptionMessage();
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithValidData_ShouldReturnCombinedData() {
        // Given
        CourseAlgorithmData courseData = createTestCourseAlgorithmData();
        AssessmentAlgorithmData assessmentData = createTestAssessmentAlgorithmData();

        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(courseData));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(assessmentData));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        AlgorithmDataParallelDTO combinedData = result.block();
        assertNotNull(combinedData);
        assertNotNull(combinedData.courseAlgorithmData());
        assertNotNull(combinedData.assessmentAlgorithmData());
        
        // Verify course data
        assertEquals(2, combinedData.courseAlgorithmData().getStudentCourses().size());
        assertEquals(1, combinedData.courseAlgorithmData().getCourseRequirements().size());
        assertEquals(1, combinedData.courseAlgorithmData().getCourseRestrictions().size());
        
        // Verify assessment data
        assertEquals(2, combinedData.assessmentAlgorithmData().getStudentAssessments().size());
        assertEquals(1, combinedData.assessmentAlgorithmData().getAssessmentRequirements().size());
        assertEquals(1, combinedData.assessmentAlgorithmData().getAssessments().size());

        verify(gradCourseService).getCourseDataForAlgorithm(eq(testStudentID), eq(testException));
        verify(gradAssessmentService).getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException));
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithEmptyCourseData_ShouldReturnCombinedData() {
        // Given
        CourseAlgorithmData emptyCourseData = createEmptyCourseAlgorithmData();
        AssessmentAlgorithmData assessmentData = createTestAssessmentAlgorithmData();

        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(emptyCourseData));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(assessmentData));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        AlgorithmDataParallelDTO combinedData = result.block();
        assertNotNull(combinedData);
        assertNotNull(combinedData.courseAlgorithmData());
        assertNotNull(combinedData.assessmentAlgorithmData());
        
        // Verify empty course data
        assertTrue(combinedData.courseAlgorithmData().getStudentCourses().isEmpty());
        assertTrue(combinedData.courseAlgorithmData().getCourseRequirements().isEmpty());
        assertTrue(combinedData.courseAlgorithmData().getCourseRestrictions().isEmpty());
        
        // Verify assessment data is still present
        assertEquals(2, combinedData.assessmentAlgorithmData().getStudentAssessments().size());
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithEmptyAssessmentData_ShouldReturnCombinedData() {
        // Given
        CourseAlgorithmData courseData = createTestCourseAlgorithmData();
        AssessmentAlgorithmData emptyAssessmentData = createEmptyAssessmentAlgorithmData();

        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(courseData));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(emptyAssessmentData));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        AlgorithmDataParallelDTO combinedData = result.block();
        assertNotNull(combinedData);
        assertNotNull(combinedData.courseAlgorithmData());
        assertNotNull(combinedData.assessmentAlgorithmData());
        
        // Verify course data is still present
        assertEquals(2, combinedData.courseAlgorithmData().getStudentCourses().size());
        
        // Verify empty assessment data
        assertTrue(combinedData.assessmentAlgorithmData().getStudentAssessments().isEmpty());
        assertTrue(combinedData.assessmentAlgorithmData().getAssessmentRequirements().isEmpty());
        assertTrue(combinedData.assessmentAlgorithmData().getAssessments().isEmpty());
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithBothEmptyData_ShouldReturnEmptyCombinedData() {
        // Given
        CourseAlgorithmData emptyCourseData = createEmptyCourseAlgorithmData();
        AssessmentAlgorithmData emptyAssessmentData = createEmptyAssessmentAlgorithmData();

        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(emptyCourseData));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(emptyAssessmentData));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        AlgorithmDataParallelDTO combinedData = result.block();
        assertNotNull(combinedData);
        assertNotNull(combinedData.courseAlgorithmData());
        assertNotNull(combinedData.assessmentAlgorithmData());
        
        // Verify both are empty
        assertTrue(combinedData.courseAlgorithmData().getStudentCourses().isEmpty());
        assertTrue(combinedData.assessmentAlgorithmData().getStudentAssessments().isEmpty());
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithCourseServiceError_ShouldCompleteWithError() {
        // Given
        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.error(new RuntimeException("Course service error")));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(createTestAssessmentAlgorithmData()));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        try {
            result.block();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Course service error"));
        }
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithAssessmentServiceError_ShouldCompleteWithError() {
        // Given
        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.just(createTestCourseAlgorithmData()));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.error(new RuntimeException("Assessment service error")));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        try {
            result.block();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Assessment service error"));
        }
    }

    @Test
    public void testFetchAlgorithmRequiredData_WithBothServicesError_ShouldCompleteWithError() {
        // Given
        when(gradCourseService.getCourseDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.error(new RuntimeException("Course service error")));
        when(gradAssessmentService.getAssessmentDataForAlgorithm(eq(testStudentID), eq(testException)))
            .thenReturn(Mono.error(new RuntimeException("Assessment service error")));

        // When
        Mono<AlgorithmDataParallelDTO> result = parallelDataFetch.fetchAlgorithmRequiredData(testStudentID, testException);

        // Then
        try {
            result.block();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Course service error"));
        }
    }

    // Helper methods
    private CourseAlgorithmData createTestCourseAlgorithmData() {
        StudentCourse course1 = new StudentCourse();
        course1.setCourseCode("MATH10");
        course1.setCourseName("Mathematics 10");

        StudentCourse course2 = new StudentCourse();
        course2.setCourseCode("SCI10");
        course2.setCourseName("Science 10");

        CourseRequirement requirement = new CourseRequirement();
        requirement.setCourseRequirementId(UUID.randomUUID());
        requirement.setCourseCode("MATH10");

        CourseRestriction restriction = new CourseRestriction();
        restriction.setCourseRestrictionId(UUID.randomUUID());
        restriction.setMainCourse("MATH10");

        return new CourseAlgorithmData(
            java.util.Arrays.asList(course1, course2),
            List.of(requirement),
            List.of(restriction)
        );
    }

    private CourseAlgorithmData createEmptyCourseAlgorithmData() {
        return new CourseAlgorithmData(
            java.util.Collections.emptyList(),
            java.util.Collections.emptyList(),
            java.util.Collections.emptyList()
        );
    }

    private AssessmentAlgorithmData createTestAssessmentAlgorithmData() {
        StudentAssessment assessment1 = new StudentAssessment();
        assessment1.setAssessmentCode("MATH10");
        assessment1.setProficiencyScore(85.5);

        StudentAssessment assessment2 = new StudentAssessment();
        assessment2.setAssessmentCode("SCI10");
        assessment2.setProficiencyScore(92.0);

        AssessmentRequirement requirement = new AssessmentRequirement();
        requirement.setAssessmentRequirementId(UUID.randomUUID());
        requirement.setAssessmentCode("MATH10");

        Assessment assessment = new Assessment();
        assessment.setAssessmentCode("MATH10");
        assessment.setAssessmentName("Mathematics 10");

        return new AssessmentAlgorithmData(
            java.util.Arrays.asList(assessment1, assessment2),
            List.of(requirement),
            List.of(assessment)
        );
    }

    private AssessmentAlgorithmData createEmptyAssessmentAlgorithmData() {
        return new AssessmentAlgorithmData(
            java.util.Collections.emptyList(),
            java.util.Collections.emptyList(),
            java.util.Collections.emptyList()
        );
    }
}
