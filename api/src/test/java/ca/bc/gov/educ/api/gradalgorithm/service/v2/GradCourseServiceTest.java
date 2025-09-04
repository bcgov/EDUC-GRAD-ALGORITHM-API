package ca.bc.gov.educ.api.gradalgorithm.service.v2;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.constants.TopicsEnum;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.util.RestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class GradCourseServiceTest {

    @Mock
    private RestUtils restUtils;

    @InjectMocks
    private GradCourseService gradCourseService;

    private UUID testStudentID;
    private ExceptionMessage testException;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        testStudentID = UUID.randomUUID();
        testException = new ExceptionMessage();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetCourseDataForAlgorithm_WithValidData_ShouldReturnCourseAlgorithmData() throws Exception {
        // Given
        List<StudentCourse> studentCourses = createTestStudentCourses();
        List<CourseRequirement> courseRequirements = createTestCourseRequirements();
        List<CourseRestriction> courseRestrictions = createTestCourseRestrictions();
        List<String> courseCodes = Arrays.asList("MATH10", "SCI10");

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_STUDENT_API_TOPIC),
            eq(EventType.GET_STUDENT_COURSES),
            eq(String.valueOf(testStudentID)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(studentCourses);

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_COURSE_API_TOPIC),
            eq(EventType.GET_COURSE_REQUIREMENTS),
            eq(objectMapper.writeValueAsString(courseCodes)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(courseRequirements);

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_COURSE_API_TOPIC),
            eq(EventType.GET_COURSE_RESTRICTIONS),
            eq(objectMapper.writeValueAsString(courseCodes)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(courseRestrictions);

        // When
        Mono<CourseAlgorithmData> result = gradCourseService.getCourseDataForAlgorithm(testStudentID, testException);

        // Then
        CourseAlgorithmData courseData = result.block();
        assertNotNull(courseData);
        assertNotNull(courseData.getStudentCourses());
        assertNotNull(courseData.getCourseRequirements());
        assertNotNull(courseData.getCourseRestrictions());
        assertEquals(2, courseData.getStudentCourses().size());
        assertEquals(1, courseData.getCourseRequirements().size());
        assertEquals(1, courseData.getCourseRestrictions().size());
        
        // Verify that gradReqMet and gradReqMetDetail are set to empty strings
        courseData.getStudentCourses().forEach(course -> {
            assertEquals("", course.getGradReqMet());
            assertEquals("", course.getGradReqMetDetail());
        });

        verify(restUtils, times(3)).sendMessageRequest(any(), any(), any(), any(), any());
    }

    @Test
    public void testGetCourseDataForAlgorithm_WithEmptyStudentCourses_ShouldReturnEmptyData() throws Exception {
        // Given
        List<StudentCourse> emptyStudentCourses = Collections.emptyList();
        List<CourseRequirement> courseRequirements = createTestCourseRequirements();
        List<CourseRestriction> courseRestrictions = createTestCourseRestrictions();

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_STUDENT_API_TOPIC),
            eq(EventType.GET_STUDENT_COURSES),
            eq(String.valueOf(testStudentID)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(emptyStudentCourses);

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_COURSE_API_TOPIC),
            eq(EventType.GET_COURSE_REQUIREMENTS),
            eq("[]"),
            any(TypeReference.class),
            isNull()
        )).thenReturn(courseRequirements);

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_COURSE_API_TOPIC),
            eq(EventType.GET_COURSE_RESTRICTIONS),
            eq("[]"),
            any(TypeReference.class),
            isNull()
        )).thenReturn(courseRestrictions);

        // When
        Mono<CourseAlgorithmData> result = gradCourseService.getCourseDataForAlgorithm(testStudentID, testException);

        // Then
        CourseAlgorithmData courseData = result.block();
        assertNotNull(courseData);
        assertTrue(courseData.getStudentCourses().isEmpty());
        assertEquals(1, courseData.getCourseRequirements().size());
        assertEquals(1, courseData.getCourseRestrictions().size());
    }

    @Test
    public void testGetCourseDataForAlgorithm_WithDuplicateCourseCodes_ShouldDeduplicate() throws Exception {
        // Given
        List<StudentCourse> studentCoursesWithDuplicates = createTestStudentCoursesWithDuplicates();
        List<CourseRequirement> courseRequirements = createTestCourseRequirements();
        List<CourseRestriction> courseRestrictions = createTestCourseRestrictions();
        List<String> expectedCourseCodes = Arrays.asList("MATH10", "SCI10"); // Should be deduplicated

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_STUDENT_API_TOPIC),
            eq(EventType.GET_STUDENT_COURSES),
            eq(String.valueOf(testStudentID)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(studentCoursesWithDuplicates);

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_COURSE_API_TOPIC),
            eq(EventType.GET_COURSE_REQUIREMENTS),
            eq(objectMapper.writeValueAsString(expectedCourseCodes)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(courseRequirements);

        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_COURSE_API_TOPIC),
            eq(EventType.GET_COURSE_RESTRICTIONS),
            eq(objectMapper.writeValueAsString(expectedCourseCodes)),
            any(TypeReference.class),
            isNull()
        )).thenReturn(courseRestrictions);

        // When
        Mono<CourseAlgorithmData> result = gradCourseService.getCourseDataForAlgorithm(testStudentID, testException);

        // Then
        CourseAlgorithmData courseData = result.block();
        assertNotNull(courseData);
        assertEquals(3, courseData.getStudentCourses().size()); // Original courses with duplicates
    }

    @Test
    public void testGetCourseDataForAlgorithm_WithException_ShouldReturnNull() {
        // Given
        when(restUtils.sendMessageRequest(
            eq(TopicsEnum.GRAD_STUDENT_API_TOPIC),
            eq(EventType.GET_STUDENT_COURSES),
            eq(String.valueOf(testStudentID)),
            any(TypeReference.class),
            isNull()
        )).thenThrow(new RuntimeException("Service unavailable"));

        // When
        Mono<CourseAlgorithmData> result = gradCourseService.getCourseDataForAlgorithm(testStudentID, testException);

        // Then
        assertNull(result);

        // Verify exception was set
        assertEquals("Could not fetch algorithm courses data", testException.getExceptionName());
        assertTrue(testException.getExceptionDetails().contains("Service unavailable"));
    }

    @Test
    public void testPrepareCourseDataForAlgorithm_WithValidData_ShouldSetEmptyGradReqFields() {
        // Given
        CourseAlgorithmData testData = createTestCourseAlgorithmData();

        // When
        CourseAlgorithmData result = gradCourseService.prepareCourseDataForAlgorithm(testData);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getStudentCourses().size());
        
        result.getStudentCourses().forEach(course -> {
            assertEquals("", course.getGradReqMet());
            assertEquals("", course.getGradReqMetDetail());
        });
    }

    @Test
    public void testPrepareCourseDataForAlgorithm_WithNullData_ShouldReturnNull() {
        // Given
        CourseAlgorithmData nullData = null;

        // When
        CourseAlgorithmData result = gradCourseService.prepareCourseDataForAlgorithm(nullData);

        // Then
        assertNull(result);
    }

    @Test
    public void testPrepareCourseDataForAlgorithm_WithEmptyStudentCourses_ShouldReturnUnchanged() {
        // Given
        CourseAlgorithmData testData = new CourseAlgorithmData(
            Collections.emptyList(),
            createTestCourseRequirements(),
            createTestCourseRestrictions()
        );

        // When
        CourseAlgorithmData result = gradCourseService.prepareCourseDataForAlgorithm(testData);

        // Then
        assertNotNull(result);
        assertTrue(result.getStudentCourses().isEmpty());
    }

    // Helper methods
    private List<StudentCourse> createTestStudentCourses() {
        StudentCourse course1 = new StudentCourse();
        course1.setCourseCode("MATH10");
        course1.setCourseName("Mathematics 10");
        course1.setGradReqMet("OLD_VALUE");
        course1.setGradReqMetDetail("OLD_DETAIL");

        StudentCourse course2 = new StudentCourse();
        course2.setCourseCode("SCI10");
        course2.setCourseName("Science 10");
        course2.setGradReqMet("OLD_VALUE");
        course2.setGradReqMetDetail("OLD_DETAIL");

        return Arrays.asList(course1, course2);
    }

    private List<StudentCourse> createTestStudentCoursesWithDuplicates() {
        StudentCourse course1 = new StudentCourse();
        course1.setCourseCode("MATH10");
        course1.setCourseName("Mathematics 10");

        StudentCourse course2 = new StudentCourse();
        course2.setCourseCode("MATH10"); // Duplicate
        course2.setCourseName("Mathematics 10");

        StudentCourse course3 = new StudentCourse();
        course3.setCourseCode("SCI10");
        course3.setCourseName("Science 10");

        return Arrays.asList(course1, course2, course3);
    }

    private List<CourseRequirement> createTestCourseRequirements() {
        CourseRequirement requirement = new CourseRequirement();
        requirement.setCourseRequirementId(UUID.randomUUID());
        requirement.setCourseCode("MATH10");
        return List.of(requirement);
    }

    private List<CourseRestriction> createTestCourseRestrictions() {
        CourseRestriction restriction = new CourseRestriction();
        restriction.setCourseRestrictionId(UUID.randomUUID());
        restriction.setMainCourse("MATH10");
        return List.of(restriction);
    }

    private CourseAlgorithmData createTestCourseAlgorithmData() {
        List<StudentCourse> studentCourses = createTestStudentCourses();
        return new CourseAlgorithmData(
            studentCourses,
            createTestCourseRequirements(),
            createTestCourseRestrictions()
        );
    }
}
