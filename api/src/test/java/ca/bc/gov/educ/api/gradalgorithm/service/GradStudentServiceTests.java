package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStudentServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradStudentServiceTests.class);
    private static final String CLASS_NAME = GradStudentServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradStudentService gradStudentService;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.grad-student-api.get-student-by-pen.url}")
    private String getStudentByPenUrl;

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
    public void getStudentDemographicsTest() {
        LOG.debug("<{}.getStudentDemographicsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "12312123123";
        String studentID = new UUID(1, 1).toString();
        String accessToken = "accessToken";

        List<GradSearchStudent> gradSearchStudents = new ArrayList();
        GradSearchStudent gradSearchStudentResponse = new GradSearchStudent();
        gradSearchStudentResponse.setPen(pen);
        gradSearchStudentResponse.setLegalFirstName("JOHN");
        gradSearchStudentResponse.setLegalLastName("SILVER");
        gradSearchStudents.add(gradSearchStudentResponse);

        ParameterizedTypeReference<List<GradSearchStudent>> responseType = new ParameterizedTypeReference<List<GradSearchStudent>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentByPenUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(gradSearchStudents));

        GradSearchStudent result = gradStudentService.getStudentDemographics(UUID.fromString(studentID), accessToken);
        assertNotNull(result);
        LOG.debug(">getStudentDemographicsTest");
    }
}
