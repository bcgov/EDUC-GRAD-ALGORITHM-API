package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmRules;
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
public class GradCommonServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradCommonServiceTests.class);
    private static final String CLASS_NAME = GradCommonServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradCommonService gradCommonService;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.gradalgorithm-api.grad-common-api.algorithm-rules-main-grad-program.url}")
    private String getAlgorithmRulesMainGradProgramUrl;

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
    public void getGradAlgorithmRulesTest() {
        LOG.debug("<{}.getGradAlgorithmRulesTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String gradProgram = "GRAD_PROGRAM_1";
        String accessToken = "accessToken";

        List<GradAlgorithmRules> gradAlgorithmRules = new ArrayList();
        GradAlgorithmRules algorithmRules = new GradAlgorithmRules();
        algorithmRules.setId(UUID.randomUUID());
        gradAlgorithmRules.add(algorithmRules);

        ParameterizedTypeReference<List<GradAlgorithmRules>> responseType = new ParameterizedTypeReference<List<GradAlgorithmRules>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getAlgorithmRulesMainGradProgramUrl, gradProgram))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(gradAlgorithmRules));

        List<GradAlgorithmRules> result = gradCommonService.getGradAlgorithmRules(gradProgram, accessToken);
        assertNotNull(result);
        LOG.debug(">getGradAlgorithmRulesTest");
    }
}
