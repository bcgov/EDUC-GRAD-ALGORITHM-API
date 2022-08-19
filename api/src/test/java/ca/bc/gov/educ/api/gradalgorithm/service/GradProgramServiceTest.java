package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradProgramServiceTest extends EducGradAlgorithmTestBase {

    @Autowired GradProgramService gradProgramService;

    @Autowired ExceptionMessage exception;
    
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
    }
    
    @Test
    public void testGetProgramDataForAlgorithm_withexception() throws Exception {
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
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));
    }
    
    
    @Test
    public void testGetOptionalProgramID() {
        String accessToken = "accessToken";
        String gradProgram = "2018-EN";
        String gradOptionalProgram = "FI";
        UUID id = new  UUID(1, 1);      
        OptionalProgram op = new OptionalProgram();
        op.setOptionalProgramID(id);
        op.setOptionalProgramName("adsad");
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getOptionalProgram(), gradProgram,gradOptionalProgram))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(op));
        
        gradProgramService.getOptionalProgramID(gradProgram,gradOptionalProgram, accessToken);
    }
}
