package ca.bc.gov.educ.api.gradalgorithm.service.v2;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.constants.TopicsEnum;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentStudentListItem;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentAssessmentCacheService;
import ca.bc.gov.educ.api.gradalgorithm.util.RestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GradAssessmentServiceTest {

    @Mock
    private StudentAssessmentCacheService studentAssessmentCacheService;

    @Mock
    private GradProgramService gradProgramCacheService;

    @Mock
    private RestUtils restUtils;

    @InjectMocks
    private GradAssessmentService gradAssessmentService;

    private UUID testStudentID;
    private ExceptionMessage testException;

    @Before
    public void setUp() {
        testStudentID = UUID.randomUUID();
        testException = new ExceptionMessage();
    }

    @Test
    public void testGetAssessmentDataForAlgorithm_WithValidData_ShouldReturnAssessmentAlgorithmData() {
        // Given
        List<AssessmentStudentListItem> studentAssessments = createTestStudentAssessments();
        List<AssessmentRequirement> assessmentRequirements = createTestAssessmentRequirements();
        List<Assessment> assessments = createTestAssessments();

        when(restUtils.<List<AssessmentStudentListItem>>sendMessageRequest(
            eq(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC),
            eq(EventType.GET_ASSESSMENT_STUDENTS),
            eq(String.valueOf(testStudentID)),
            any(),
            eq(Collections.emptyList())
        )).thenReturn(studentAssessments);

        when(gradProgramCacheService.getAllAssessmentRequirements()).thenReturn(assessmentRequirements);
        when(studentAssessmentCacheService.getAllAssessments()).thenReturn(assessments);
        when(studentAssessmentCacheService.getAssessmentNameByCode(anyString())).thenReturn("Test Assessment");

        // When
        Mono<AssessmentAlgorithmData> result = gradAssessmentService.getAssessmentDataForAlgorithm(testStudentID, testException);

        // Then
        AssessmentAlgorithmData assessmentData = result.block();
        assertNotNull(assessmentData);
        assertNotNull(assessmentData.getStudentAssessments());
        assertNotNull(assessmentData.getAssessmentRequirements());
        assertNotNull(assessmentData.getAssessments());
        assertEquals(2, assessmentData.getStudentAssessments().size());
        assertEquals(1, assessmentData.getAssessmentRequirements().size());
        assertEquals(1, assessmentData.getAssessments().size());
        
        // Verify that gradReqMet and gradReqMetDetail are set to empty strings
        assessmentData.getStudentAssessments().forEach(student -> {
            assertEquals("", student.getGradReqMet());
            assertEquals("", student.getGradReqMetDetail());
            assertNotNull(student.getAssessmentName());
        });

        verify(restUtils).sendMessageRequest(
            eq(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC),
            eq(EventType.GET_ASSESSMENT_STUDENTS),
            eq(String.valueOf(testStudentID)),
            any(),
            eq(Collections.emptyList())
        );
        verify(gradProgramCacheService).getAllAssessmentRequirements();
        verify(studentAssessmentCacheService).getAllAssessments();
        verify(studentAssessmentCacheService, times(2)).getAssessmentNameByCode(anyString());
    }

    @Test
    public void testGetAssessmentDataForAlgorithm_WithEmptyStudentAssessments_ShouldReturnEmptyData() {
        // Given
        List<AssessmentStudentListItem> emptyStudentAssessments = Collections.emptyList();
        List<AssessmentRequirement> assessmentRequirements = createTestAssessmentRequirements();
        List<Assessment> assessments = createTestAssessments();

        when(restUtils.<List<AssessmentStudentListItem>>sendMessageRequest(
            eq(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC),
            eq(EventType.GET_ASSESSMENT_STUDENTS),
            eq(String.valueOf(testStudentID)),
            any(),
            eq(Collections.emptyList())
        )).thenReturn(emptyStudentAssessments);

        when(gradProgramCacheService.getAllAssessmentRequirements()).thenReturn(assessmentRequirements);
        when(studentAssessmentCacheService.getAllAssessments()).thenReturn(assessments);

        // When
        Mono<AssessmentAlgorithmData> result = gradAssessmentService.getAssessmentDataForAlgorithm(testStudentID, testException);

        // Then
        AssessmentAlgorithmData assessmentData = result.block();
        assertNotNull(assessmentData);
        assertTrue(assessmentData.getStudentAssessments().isEmpty());
        assertEquals(1, assessmentData.getAssessmentRequirements().size());
        assertEquals(1, assessmentData.getAssessments().size());
    }

    @Test
    public void testGetAssessmentDataForAlgorithm_WithException_ShouldReturnNull() {
        // Given
        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC),
            eq(EventType.GET_ASSESSMENT_STUDENTS),
            eq(String.valueOf(testStudentID)),
            any(),
            eq(Collections.emptyList())
        )).thenThrow(new RuntimeException("Service unavailable"));

        // When
        Mono<AssessmentAlgorithmData> result = gradAssessmentService.getAssessmentDataForAlgorithm(testStudentID, testException);

        // Then
        assertNull(result);

        // Verify exception was set
        assertEquals("Could not fetch algorithm assessment data", testException.getExceptionName());
        assertTrue(testException.getExceptionDetails().contains("Service unavailable"));
    }

    @Test
    public void testPrepareAssessmentDataForAlgorithm_WithValidData_ShouldSetEmptyGradReqFields() {
        // Given
        AssessmentAlgorithmData testData = createTestAssessmentAlgorithmData();

        // When
        AssessmentAlgorithmData result = gradAssessmentService.prepareAssessmentDataForAlgorithm(testData);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getStudentAssessments().size());
        
        result.getStudentAssessments().forEach(student -> {
            assertEquals("", student.getGradReqMet());
            assertEquals("", student.getGradReqMetDetail());
        });
    }

    @Test
    public void testPrepareAssessmentDataForAlgorithm_WithNullData_ShouldReturnNull() {
        // Given
        AssessmentAlgorithmData nullData = null;

        // When
        AssessmentAlgorithmData result = gradAssessmentService.prepareAssessmentDataForAlgorithm(nullData);

        // Then
        assertNull(result);
    }

    @Test
    public void testPrepareAssessmentDataForAlgorithm_WithEmptyStudentAssessments_ShouldReturnUnchanged() {
        // Given
        AssessmentAlgorithmData testData = new AssessmentAlgorithmData(
            Collections.emptyList(),
            createTestAssessmentRequirements(),
            createTestAssessments()
        );

        // When
        AssessmentAlgorithmData result = gradAssessmentService.prepareAssessmentDataForAlgorithm(testData);

        // Then
        assertNotNull(result);
        assertTrue(result.getStudentAssessments().isEmpty());
    }

    // Helper methods
    private List<AssessmentStudentListItem> createTestStudentAssessments() {
        AssessmentStudentListItem assessment1 = AssessmentStudentListItem.builder()
            .assessmentStudentID(UUID.randomUUID().toString())
            .assessmentTypeCode("MATH10")
            .proficiencyScore("85.5")
            .pen("123456789")
            .build();

        AssessmentStudentListItem assessment2 = AssessmentStudentListItem.builder()
            .assessmentStudentID(UUID.randomUUID().toString())
            .assessmentTypeCode("SCI10")
            .proficiencyScore("92.0")
            .pen("123456789")
            .build();

        return Arrays.asList(assessment1, assessment2);
    }

    private List<AssessmentRequirement> createTestAssessmentRequirements() {
        AssessmentRequirement requirement = new AssessmentRequirement();
        requirement.setAssessmentRequirementId(UUID.randomUUID());
        requirement.setAssessmentCode("MATH10");
        return List.of(requirement);
    }

    private List<Assessment> createTestAssessments() {
        Assessment assessment = new Assessment();
        assessment.setAssessmentCode("MATH10");
        assessment.setAssessmentName("Mathematics 10");
        assessment.setLanguage("EN");
        return List.of(assessment);
    }

    private AssessmentAlgorithmData createTestAssessmentAlgorithmData() {
        List<StudentAssessment> studentAssessments = Arrays.asList(
            createTestStudentAssessment("MATH10", "85.5"),
            createTestStudentAssessment("SCI10", "92.0")
        );
        
        return new AssessmentAlgorithmData(
            studentAssessments,
            createTestAssessmentRequirements(),
            createTestAssessments()
        );
    }

    private StudentAssessment createTestStudentAssessment(String code, String score) {
        StudentAssessment student = new StudentAssessment();
        student.setAssessmentCode(code);
        student.setProficiencyScore(Double.parseDouble(score));
        student.setGradReqMet("OLD_VALUE");
        student.setGradReqMetDetail("OLD_DETAIL");
        return student;
    }
}
