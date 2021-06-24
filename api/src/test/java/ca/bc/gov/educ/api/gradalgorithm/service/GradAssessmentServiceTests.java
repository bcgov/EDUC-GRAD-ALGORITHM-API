package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.Assessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentRequirement;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentRequirements;
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
@SpringBootTest
@ActiveProfiles("test")
public class GradAssessmentServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradAssessmentServiceTests.class);
    private static final String CLASS_NAME = GradAssessmentServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradAssessmentService gradAssessmentService;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.assessment-api.assessment-api-base.url}")
    private String getAssessmentBaseUrl;
    @Value("${endpoint.assessment-api.assessment-api-requirement-assessments.url}")
    private String getAssessmentRequirementsUrl;

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
    public void getAllAssessmentsTest() {
        LOG.debug("<{}.getAllAssessmentsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String accessToken = "accessToken";

        List<Assessment> entity = new ArrayList();

        ParameterizedTypeReference<List<Assessment>> speciaalProgramResponseType = new ParameterizedTypeReference<List<Assessment>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getAssessmentBaseUrl)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(speciaalProgramResponseType)).thenReturn(Mono.just(entity));

        List<Assessment> result = gradAssessmentService.getAllAssessments(accessToken);
        assertNotNull(result);
        LOG.debug(">getAllAssessmentsTest");
    }

    @Test
    public void getAllAssessmentRequirementsTest() {
        LOG.debug("<{}.getAllAssessmentRequirementsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String accessToken = "accessToken";

        AssessmentRequirements entity = new AssessmentRequirements();
        List<AssessmentRequirement> assessmentRequirementList = new ArrayList<>();
        entity.setAssessmentRequirementList(assessmentRequirementList);

        List<StudentAssessment> studentAssessmentList = new ArrayList<>();
        StudentAssessment assessment = new StudentAssessment();
        assessment.setAssessmentCode("ASSESSMENT_1");
        assessment.setAssessmentName("My Assessment");
        studentAssessmentList.add(assessment);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(getAssessmentRequirementsUrl)).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(AssessmentRequirements.class)).thenReturn(Mono.just(entity));

        AssessmentRequirements result = gradAssessmentService.getAllAssessmentRequirements(studentAssessmentList, accessToken);
        assertNotNull(result);
        LOG.debug(">getAllAssessmentRequirementsTest");
    }
}
