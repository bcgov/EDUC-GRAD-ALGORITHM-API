package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.function.Consumer;

import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradCourseServiceTest extends EducGradAlgorithmTestBase {

    @Autowired GradCourseService gradCourseService;
    @Autowired ExceptionMessage exception;
    @MockBean(name = "algorithmApiClient")
    @Qualifier("algorithmApiClient")
    WebClient algorithmApiClient;
    @MockBean GradProgramService gradProgramService;
    @MockBean GradSchoolService gradSchoolService;
    @MockBean StudentGraduationService studentGraduationService;
    @Autowired GradAlgorithmAPIConstants constants;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @BeforeClass
    public static void setup() {

    }

    @After
    public void tearDown() {

    }

    @Before
    public void init() {
        this.gradProgramService.init();
        this.gradSchoolService.init();
        this.studentGraduationService.init();
        openMocks(this);
    }

    @Test
    public void testGetCourseDataForAlgorithm() throws Exception {
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
        String pen = "1312311231";

        when(this.algorithmApiClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCourseData(), pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CourseAlgorithmData.class)).thenReturn(Mono.just(courseAlgorithmData));

        Mono<CourseAlgorithmData> res = gradCourseService.getCourseDataForAlgorithm(pen, exception);
        assertNotNull(res.block());
    }

    @Test
    public void testGetCourseDataForAlgorithm_checkThrowException() {
        String pen = "1312311231";
        when(this.algorithmApiClient.get()).thenThrow(new RuntimeException(""));
        Mono<CourseAlgorithmData> courseDataForAlgorithm = gradCourseService.getCourseDataForAlgorithm(pen, exception);

        assertNull(courseDataForAlgorithm);
    }

    @Test
    public void testPrepareCourseDataForAlgorithm() throws Exception {
        CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
        CourseAlgorithmData res = gradCourseService.prepareCourseDataForAlgorithm(courseAlgorithmData);
        assertNotNull(res);
    }

    @Test
    public void testPrepareCourseDataForAlgorithm_withNullRequirementsAndRestrictions() throws Exception {
        CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
        courseAlgorithmData.setCourseRequirements(null);
        courseAlgorithmData.setCourseRestrictions(null);
        CourseAlgorithmData res = gradCourseService.prepareCourseDataForAlgorithm(courseAlgorithmData);
        assertNotNull(res);
    }

    @Test
    public void testPrepareCourseDataForAlgorithm_withEmptyData() {
        CourseAlgorithmData courseAlgorithmData = new CourseAlgorithmData(
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        CourseAlgorithmData res = gradCourseService.prepareCourseDataForAlgorithm(courseAlgorithmData);
        assertNotNull(res);
    }

    @Test
    public void testPrepareCourseDataForAlgorithm_withNullData() {
        CourseAlgorithmData res = gradCourseService.prepareCourseDataForAlgorithm(null);
        assertNull(res);
    }
}
