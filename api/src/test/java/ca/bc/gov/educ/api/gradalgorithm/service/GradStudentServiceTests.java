package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentAlgorithmData;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStudentServiceTests extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradStudentServiceTests.class);
    private static final String CLASS_NAME = GradStudentServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired GradStudentService gradStudentService;
    @MockBean WebClient webClient;
    @Autowired GradAlgorithmAPIConstants constants;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.ResponseSpec responseMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;

    @BeforeClass
    public static void setup() {

    }

    @After
    public void tearDown() {

    }

    @Before
    public void init() {
        openMocks(this);
    }

    @Test
    public void getStudentDemographicsTest() {
        LOG.debug("<{}.getStudentDemographicsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "12312123123";
        String studentID = new UUID(1, 1).toString();
        String accessToken = "accessToken";

        GradSearchStudent gradSearchStudentResponse = new GradSearchStudent();
        gradSearchStudentResponse.setPen(pen);
        gradSearchStudentResponse.setLegalFirstName("JOHN");
        gradSearchStudentResponse.setLegalLastName("SILVER");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentDemographics(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradSearchStudent.class)).thenReturn(Mono.just(gradSearchStudentResponse));

        GradSearchStudent result = gradStudentService.getStudentDemographics(UUID.fromString(studentID), accessToken);
        assertNotNull(result);
        LOG.debug(">getStudentDemographicsTest");
    }
    
    @Test
    public void testgetGradStudentData() throws Exception {
    	
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	
    	String studentID = new UUID(1, 1).toString();
    	String accessToken = "accessToken";
    	when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradStudentAlgorithmData(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradStudentAlgorithmData.class)).thenReturn(Mono.just(gradStudentAlgorithmData));
        
        gradStudentService.getGradStudentData(UUID.fromString(studentID), accessToken, new ExceptionMessage());
         
    }
    
    @Test
    public void testgetGradStudentData_withexception() throws Exception {
    	
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	
    	String studentID = new UUID(1, 1).toString();
    	String accessToken = "accessToken";
    	when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradStudentAlgorithmData(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));
        
        gradStudentService.getGradStudentData(UUID.fromString(studentID), accessToken, new ExceptionMessage());
         
    }
}
