package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.text.SimpleDateFormat;
import java.util.UUID;
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
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class StudentGraduationServiceTest extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(StudentGraduationServiceTest.class);
    private static final String CLASS_NAME = StudentGraduationServiceTest.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    StudentGraduationService studentGraduationService;
    
    @Autowired
    private ExceptionMessage exception;

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
    public void testGetAllAlgorithmData() throws Exception {
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
        String accessToken = "accessToken";
        String programCode = "2018-EN";       

        
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata/"+programCode)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(StudentGraduationAlgorithmData.class)).thenReturn(Mono.just(studentGraduationAlgorithmData));
        
        studentGraduationService.getAllAlgorithmData(programCode, accessToken,exception);
    }
    
    @Test
    public void testGetAllAlgorithmData_withException() throws Exception {
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
        String accessToken = "accessToken";
        String programCode = "2018-EN";       

        
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata/"+programCode)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));
        
        studentGraduationService.getAllAlgorithmData(programCode, accessToken,exception);
    }
    
    @Test
    public void testGetGradMessages() throws Exception {
        String accessToken = "accessToken";
        String programCode = "2018-EN";       
        String msgType = "GRADUATED";
        
        TranscriptMessage msg = new TranscriptMessage();
        msg.setAdIBProgramMessage("dsada");
        msg.setCareerProgramMessage("asdsa");
        msg.setGradDateMessage("asddad");
        msg.setGradMainMessage("ASdasdad");
        msg.setHonourNote("asda");
        msg.setMessageTypeCode(msgType);
        msg.setProgramCadre("Adasd");
        msg.setProgramCode(programCode);
        msg.setTranscriptMessageID(new UUID(1, 1));
        
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGraduationMessage(),programCode,msgType))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(TranscriptMessage.class)).thenReturn(Mono.just(msg));
        
        studentGraduationService.getGradMessages(programCode,msgType, accessToken,exception);
    }
    
    @Test
    public void testGetGradMessages_withException() throws Exception {
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
        String accessToken = "accessToken";
        String programCode = "2018-EN";
        String msgType = "GRADUATED";
        
        
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGraduationMessage(),programCode,msgType))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));
        
        studentGraduationService.getGradMessages(programCode,msgType, accessToken,exception);
    }
}
