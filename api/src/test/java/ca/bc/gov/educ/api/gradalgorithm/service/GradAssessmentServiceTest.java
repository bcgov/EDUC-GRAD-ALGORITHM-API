package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAssessmentServiceTest extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradAssessmentServiceTest.class);
    private static final String CLASS_NAME = GradAssessmentServiceTest.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradAssessmentService gradAssessmentService;

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
    public void testGetAssessmentDataForAlgorithm() throws Exception {
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
        String accessToken = "accessToken";
        String pen = "1312311231";       
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getAssessmentData(),pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(AssessmentAlgorithmData.class)).thenReturn(Mono.just(assessmentAlgorithmData));
        
        gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken);
    }
}
