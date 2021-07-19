package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
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
public class GradProgramServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradProgramServiceTests.class);
    private static final String CLASS_NAME = GradProgramServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradProgramService gradProgramService;

    @MockBean
    WebClient webClient;

    @Value("${endpoint.gradalgorithm-api.grad-program-management-api.program_management_base.url}")
    private String programManagementBaseUrl;

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
    public void getProgramRulesTest() {
        LOG.debug("<{}.getProgramRulesTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String programCode = "PROG_CODE_1";
        String accessToken = "accessToken";

        List<GradProgramRule> entity = new ArrayList<>();

        ParameterizedTypeReference<List<GradProgramRule>> responseType = new ParameterizedTypeReference<List<GradProgramRule>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/programrules?programCode=" + programCode)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(entity));

        List<GradProgramRule> result = gradProgramService.getProgramRules(programCode, accessToken);
        assertNotNull(result);
        LOG.debug(">getProgramRulesTest");
    }

    @Test
    public void getSpecialProgramRulesTest() {
        LOG.debug("<{}.getSpecialProgramRulesTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String gradProgram = "GRAD_PROG_1";
        String gradSpecialProgram = "GRAD_SPEC__PROG_1";
        String accessToken = "accessToken";

        List<GradSpecialProgramRule> entity = new ArrayList<>();

        ParameterizedTypeReference<List<GradSpecialProgramRule>> responseType = new ParameterizedTypeReference<List<GradSpecialProgramRule>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/specialprogramrules/" + gradProgram + "/" + gradSpecialProgram)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(entity));

        List<GradSpecialProgramRule> result = gradProgramService.getSpecialProgramRules(gradProgram, gradSpecialProgram, accessToken);
        assertNotNull(result);
        LOG.debug(">getSpecialProgramRulesTest");
    }

    @Test
    public void getSpecialProgramIDTest() {
        LOG.debug("<{}.getSpecialProgramRulesTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String gradProgram = "GRAD_PROG_1";
        String gradSpecialProgram = "GRAD_SPEC__PROG_1";
        String accessToken = "accessToken";

        GradSpecialProgram entity = new GradSpecialProgram();
        entity.setId(UUID.randomUUID());

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/specialprograms/" + gradProgram + "/" + gradSpecialProgram)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradSpecialProgram.class)).thenReturn(Mono.just(entity));

        UUID result = gradProgramService.getSpecialProgramID(gradProgram, gradSpecialProgram, accessToken);
        assertNotNull(result);
        LOG.debug(">getSpecialProgramRulesTest");
    }
}
