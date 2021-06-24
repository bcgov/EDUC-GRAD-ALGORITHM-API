package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStudentAssessmentsServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradStudentAssessmentsServiceTests.class);
    private static final String CLASS_NAME = GradStudentAssessmentsServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradStudentAssessmentService gradStudentAssessmentService;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.student-assessment-api.get-student-assessment-by-pen.url}")
    private String getStudentAssessmentUrl;

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
    public void getStudentAssessmentsTest() {
        LOG.debug("<{}.getStudentCourseTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "12312123123";
        String accessToken = "accessToken";

        StudentAssessment[] studentAssessments = new StudentAssessment[1];
        studentAssessments[0] = new StudentAssessment();
        studentAssessments[0].setAssessmentCode("ASSESSMENT_1");
        studentAssessments[0].setAssessmentName("Assessment 1");

        ParameterizedTypeReference<StudentAssessment[]> responseType = new ParameterizedTypeReference<StudentAssessment[]>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentAssessmentUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(studentAssessments));

        List<StudentAssessment> result = gradStudentAssessmentService.getAllAssessmentsForAStudent(pen, accessToken);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        LOG.debug(">getStudentCourseTest");
    }
}
