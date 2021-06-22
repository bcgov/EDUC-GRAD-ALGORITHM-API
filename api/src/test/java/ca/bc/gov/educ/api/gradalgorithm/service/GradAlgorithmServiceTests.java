package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmApiApplication;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EducGradAlgorithmApiApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
public class GradAlgorithmServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradAlgorithmServiceTests.class);
    private static final String CLASS_NAME = GradAlgorithmServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradAlgorithmService gradAlgorithmService;

    @MockBean
    WebClient webClient;
    @Autowired
    private GradAlgorithmAPIConstants constants;

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

    @Value("${endpoint.grad-student-api.get-student-by-pen.url}")
    private String getStudentByPenUrl;
    @Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
    private String getStudentCourseByPenUrl;
    @Value("${endpoint.student-assessment-api.get-student-assessment-by-pen.url}")
    private String getStudentAssessmentUrl;

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

    //@Test
    public void graduateStudentTest() {
        LOG.debug("<{}.graduateStudentTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "12312123123";
        String programCode="2018-EN";
        String accessToken = "accessToken";

        /** Start Get Student Demographics **/
        List<GradSearchStudent> gradSearchStudents = new ArrayList();
        GradSearchStudent gradSearchStudentResponse = new GradSearchStudent();
        gradSearchStudentResponse.setPen(pen);
        gradSearchStudentResponse.setLegalFirstName("JOHN");
        gradSearchStudentResponse.setLegalLastName("SILVER");
        gradSearchStudents.add(gradSearchStudentResponse);

        ParameterizedTypeReference<List<GradSearchStudent>> studentResponseType = new ParameterizedTypeReference<List<GradSearchStudent>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentByPenUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(gradSearchStudents));

        /** End Get Student Demographics **/

        /** Start Get All Courses for a Student **/

        StudentCourse[] studentCourse = new StudentCourse[1];
        studentCourse[0] = new StudentCourse();
        studentCourse[0].setCourseCode("COURSE1");
        studentCourse[0].setCourseName("Course 1");

        ParameterizedTypeReference<StudentCourse[]> courseResponseType = new ParameterizedTypeReference<StudentCourse[]>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentCourseByPenUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(courseResponseType)).thenReturn(Mono.just(studentCourse));

        /** End Get All Courses for a Student **/

        /** Start Get All Assessments for a Student **/

        StudentAssessment[] studentAssessments = new StudentAssessment[1];
        studentAssessments[0] = new StudentAssessment();
        studentAssessments[0].setAssessmentCode("ASSESSMENT_1");
        studentAssessments[0].setAssessmentName("Assessment 1");

        ParameterizedTypeReference<StudentAssessment[]> assessmentResponseType = new ParameterizedTypeReference<StudentAssessment[]>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentAssessmentUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(assessmentResponseType)).thenReturn(Mono.just(studentAssessments));

        /** End Get All Assessments for a Student **/

        /** Start Get All course Requirements **/

        /** End Get All course Requirements **/

        /** Start Get All Assessment Requirements **/

        /** End Get All Assessment Requirements **/

        /** Start Get All Grad Letter Grades **/

        /** End Get All Grad Letter Grades **/

        /** Start Get All Grad Special Cases **/

        /** End Get All Grad Special Cases **/

        /** Start Get Grad Algorithm Rules from the DB **/

        /** End Get Grad Algorithm Rules from the DB **/

        /** Start Get All course restrictions **/

        /** End Get All course restrictions **/

        /** Start Get all Grad Program Rules **/

        /** End Get all Grad Program Rules **/

        /** Start Get all Grad Program Rules **/

        /** End Get all Grad Program Rules **/


        GraduationData gradData = gradAlgorithmService.graduateStudent(pen, programCode, false, accessToken);
        assertNotNull(gradData);
        LOG.debug(">graduateStudentTest");
    }
}
