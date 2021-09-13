package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
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
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradProgramServiceTest extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradProgramServiceTest.class);
    private static final String CLASS_NAME = GradProgramServiceTest.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradProgramService gradProgramService;

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
    public void testGetProgramDataForAlgorithm() throws Exception {
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
        String accessToken = "accessToken";
        String programCode = "2018-EN";
        String optionalProgramCode = "";
        String url = constants.getProgramData() + "programCode=%s";
    	if(StringUtils.isNotBlank(optionalProgramCode)) {
    		url = url + "&optionalProgramCode=%s";
    	}       
        
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(url,programCode,optionalProgramCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgramAlgorithmData.class)).thenReturn(Mono.just(programAlgorithmData));
        
        gradProgramService.getProgramDataForAlgorithm(programCode,optionalProgramCode, accessToken,exception);
    }
    
    @Test
    public void testGetSpecialProgramID() {
        String accessToken = "accessToken";
        String gradProgram = "2018-EN";
        String gradSpecialProgram = "FI";
        UUID id = new  UUID(1, 1);      
        OptionalProgram op = new OptionalProgram();
        op.setOptionalProgramID(id);
        op.setOptionalProgramName("adsad");
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getOptionalProgram(), gradProgram,gradSpecialProgram))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(op));
        
        gradProgramService.getSpecialProgramID(gradProgram,gradSpecialProgram, accessToken,exception);
    }
}
