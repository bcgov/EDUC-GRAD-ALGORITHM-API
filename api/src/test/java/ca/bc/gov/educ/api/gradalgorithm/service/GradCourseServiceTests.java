package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmApiApplication;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EducGradAlgorithmApiApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
public class GradCourseServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradCourseServiceTests.class);
    private static final String CLASS_NAME = GradCourseServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradCourseService gradCourseService;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.course-api.course-requirements-api.url}")
    private String getCourseRequirementsUrl;
    @Value("${endpoint.course-api.course-restriction-api.url}")
    private String getCourseRestrictionUrl;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;

    @BeforeClass
    public static void setup() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Before
    public void init() throws Exception {
        openMocks(this);
    }

    @Test
    public void getAllCourseRequirementsTest() {
        LOG.debug("<{}.getAllCourseRequirementsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String accessToken = "accessToken";

        CourseRequirements entity = new CourseRequirements();
        List<CourseRequirement> courseRequirementList = new ArrayList<>();
        entity.setCourseRequirementList(courseRequirementList);

        List<StudentCourse> studentCourseList = new ArrayList<>();
        StudentCourse course = new StudentCourse();
        course.setCourseCode("COURSE_1");
        course.setCourseName("My Course");
        studentCourseList.add(course);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(getCourseRequirementsUrl + "/course-list")).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CourseRequirements.class)).thenReturn(Mono.just(entity));

        CourseRequirements result = gradCourseService.getAllCourseRequirements(studentCourseList, accessToken);
        assertNotNull(result);
        LOG.debug(">getAllCourseRequirementsTest");
    }

    @Test
    public void getAllCourseRestrictionsTest() {
        LOG.debug("<{}.getAllCourseRestrictionsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String accessToken = "accessToken";

        CourseRestrictions entity = new CourseRestrictions();
        List<CourseRestriction> courseRestrictionList = new ArrayList<>();
        entity.setCourseRestrictions(courseRestrictionList);

        List<StudentCourse> studentCourseList = new ArrayList<>();
        StudentCourse course = new StudentCourse();
        course.setCourseCode("COURSE_1");
        course.setCourseName("My Course");
        studentCourseList.add(course);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(getCourseRestrictionUrl + "/course-list")).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CourseRestrictions.class)).thenReturn(Mono.just(entity));

        CourseRestrictions result = gradCourseService.getAllCourseRestrictions(studentCourseList, accessToken);
        assertNotNull(result);
        LOG.debug(">getAllCourseRestrictionsTest");
    }
}
