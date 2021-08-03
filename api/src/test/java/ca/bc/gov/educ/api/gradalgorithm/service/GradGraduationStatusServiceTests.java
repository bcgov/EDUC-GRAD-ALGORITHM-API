package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
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
public class GradGraduationStatusServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradGraduationStatusServiceTests.class);
    private static final String CLASS_NAME = GradGraduationStatusServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradGraduationStatusService gradGraduationStatusService;

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
    public void getStudentGraduationStatusTest() {
        LOG.debug("<{}.getStudentSpecialProgramsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "1111111111";
        UUID studentID = UUID.randomUUID();
        String accessToken = "accessToken";

        GradAlgorithmGraduationStudentRecord entity = new GradAlgorithmGraduationStudentRecord();
        entity.setPen(pen);
        entity.setStudentID(studentID);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGraduationStudentRecord(), studentID.toString()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradAlgorithmGraduationStudentRecord.class)).thenReturn(Mono.just(entity));

        GradAlgorithmGraduationStudentRecord result = gradGraduationStatusService.getStudentGraduationStatus(studentID.toString(), accessToken);
        assertNotNull(result);
        LOG.debug(">getStudentSpecialProgramsTest");
    }

    @Test
    public void getStudentSpecialProgramsByIdTest() {
        LOG.debug("<{}.getStudentSpecialProgramsByIdTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "1111111111";
        UUID studentID = UUID.randomUUID();
        String accessToken = "accessToken";

        List<StudentOptionalProgram> entity = new ArrayList<>();

        ParameterizedTypeReference<List<StudentOptionalProgram>> specialProgramResponseType = new ParameterizedTypeReference<List<StudentOptionalProgram>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentOptionalPrograms(), studentID.toString()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(specialProgramResponseType)).thenReturn(Mono.just(entity));

        List<StudentOptionalProgram> result = gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken);
        assertNotNull(result);
        LOG.debug(">getStudentSpecialProgramsByIdTest");
    }
}
