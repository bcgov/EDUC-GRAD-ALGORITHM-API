package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradRuleProcessorServiceTests extends EducGradAlgorithmTestBase {

    private static final String CLASS_NAME = GradRuleProcessorServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired GradRuleProcessorService gradRuleProcessorService;
    @Autowired ExceptionMessage exception;
    @MockBean(name = "algorithmApiClient")
    @Qualifier("algorithmApiClient")
    WebClient algorithmApiClient;
    @MockBean GradProgramService gradProgramService;
    @MockBean GradSchoolService gradSchoolService;
    @MockBean StudentGraduationService studentGraduationService;

    @Value("${endpoint.rule-engine-api.run-grad-algorithm-rules.url}")
    private String ruleEngineRunGradAlgorithmRulesUrl;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
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
        this.gradProgramService.init();
        this.gradSchoolService.init();
        this.studentGraduationService.init();
        openMocks(this);
    }

    @Test
    public void getRuleProcessorTest() throws Exception {
        log.debug("<{}.getRuleProcessorTest at {}", CLASS_NAME, dateFormat.format(new Date()));

        RuleProcessorData ruleProcessorData = createRuleProcessorData("json/ruleProcessorData.json");

        when(this.algorithmApiClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(ruleEngineRunGradAlgorithmRulesUrl)).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(RuleProcessorData.class)).thenReturn(Mono.just(ruleProcessorData));

        RuleProcessorData result = gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, exception);
        assertNotNull(result);
        log.debug(">getRuleProcessorTest");
    }
    
    @Test
    public void testgetGradStudentData_withexception() throws Exception {
    	
        RuleProcessorData ruleProcessorData = createRuleProcessorData("json/ruleProcessorData.json");
        
    	when(this.algorithmApiClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(ruleEngineRunGradAlgorithmRulesUrl)).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));
        
        RuleProcessorData result = gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, exception);
        assertNull(result);
         
    }
}
