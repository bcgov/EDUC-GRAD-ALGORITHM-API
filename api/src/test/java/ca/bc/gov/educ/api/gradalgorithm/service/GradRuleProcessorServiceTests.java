package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmApiApplication;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EducGradAlgorithmApiApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
public class GradRuleProcessorServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradRuleProcessorServiceTests.class);
    private static final String CLASS_NAME = GradRuleProcessorServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradRuleProcessorService gradRuleProcessorService;
    @Autowired
    JsonTransformer jsonTransformer;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.rule-engine-api.base-url}")
    private String ruleEngineBaseUrl;
    @Value("${endpoint.rule-engine-api.endpoints.run-grad-algorithm-rules}")
    private String ruleEngineRunGradAlgorithmRulesUrl;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
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
    public void getRuleProcessorTest() throws Exception {
        LOG.debug("<{}.getRuleProcessorTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String accessToken = "accessToken";

        RuleProcessorData ruleProcessorData = createRuleProcessorData("json/ruleProcessorData.json");

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(ruleEngineBaseUrl + "/" + ruleEngineRunGradAlgorithmRulesUrl)).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(RuleProcessorData.class)).thenReturn(Mono.just(ruleProcessorData));

        RuleProcessorData result = gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken);
        assertNotNull(result);
        LOG.debug(">getRuleProcessorTest");
    }

    protected RuleProcessorData createRuleProcessorData(String jsonPath) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
        String json = readInputStream(inputStream);
        return (RuleProcessorData)jsonTransformer.unmarshall(json, RuleProcessorData.class);
    }

    private String readInputStream(InputStream is) throws Exception {
        StringBuffer sb = new StringBuffer();
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
