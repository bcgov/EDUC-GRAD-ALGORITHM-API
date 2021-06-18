package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmApiApplication;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private Mono<GraduationData> monoResponseGraduationData;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;

    @Value("${endpoint.gradalgorithm-api.gradalgorithm}")
    private String gradAlgorithmEndpoint;

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
    public void graduateStudentTest() {
        LOG.debug("<{}.graduateStudentTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "12312123123";
        String programCode="2018-EN";
        String accessToken = "accessToken";

        GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = new GradAlgorithmGraduationStatus();
        gradAlgorithmGraduationStatus.setPen("123090109");
        gradAlgorithmGraduationStatus.setProgram("2018-EN");
        gradAlgorithmGraduationStatus.setProgramCompletionDate(null);
        gradAlgorithmGraduationStatus.setSchoolOfRecord("06011033");
        gradAlgorithmGraduationStatus.setStudentGrade("11");
        gradAlgorithmGraduationStatus.setStudentStatus("A");

        GraduationData graduationDataStatus = new GraduationData();
        graduationDataStatus.setDualDogwood(false);
        graduationDataStatus.setGradMessage("Not Graduated");
        graduationDataStatus.setGradStatus(gradAlgorithmGraduationStatus);
        graduationDataStatus.setGraduated(false);
        graduationDataStatus.setStudentCourses(null);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(gradAlgorithmEndpoint, pen,programCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GraduationData.class)).thenReturn(monoResponseGraduationData);
        when(this.monoResponseGraduationData.block()).thenReturn(graduationDataStatus);

        GraduationData gradData = gradAlgorithmService.graduateStudent(pen, programCode, false, accessToken);
        assertNotNull(gradData);
        LOG.debug(">graduateStudentTest");
    }
}
