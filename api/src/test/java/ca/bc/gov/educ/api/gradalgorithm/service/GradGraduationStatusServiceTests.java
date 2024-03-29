package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
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

    @Autowired GradGraduationStatusService gradGraduationStatusService;
    @Autowired ExceptionMessage exception;
    @MockBean WebClient webClient;
    @MockBean GradProgramService gradProgramService;
    @MockBean GradSchoolService gradSchoolService;
    @MockBean StudentGraduationService studentGraduationService;
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
        this.gradProgramService.init();
        this.gradSchoolService.init();
        this.studentGraduationService.init();
        openMocks(this);
    }

   
    @Test
    public void getStudentGraduationStatusTest() {
        LOG.debug("<{}.getStudentOptionalProgramsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "1111111111";
        UUID studentID = UUID.randomUUID();
        String accessToken = "accessToken";

        GradAlgorithmGraduationStudentRecord entity = new GradAlgorithmGraduationStudentRecord();
        entity.setPen(pen);
        entity.setStudentID(studentID);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGraduationStudentRecord(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradAlgorithmGraduationStudentRecord.class)).thenReturn(Mono.just(entity));

        GradAlgorithmGraduationStudentRecord result = gradGraduationStatusService.getStudentGraduationStatus(studentID.toString(), accessToken);
        assertNotNull(result);
        LOG.debug(">getStudentOptionalProgramsTest");
    }

    @Test
    public void getStudentOptionalProgramsByIdTest() {
        LOG.debug("<{}.getStudentOptionalProgramsByIdTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        UUID studentID = UUID.randomUUID();
        String accessToken = "accessToken";

        List<StudentOptionalProgram> entity = new ArrayList<>();

        ParameterizedTypeReference<List<StudentOptionalProgram>> optionalProgramResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentOptionalPrograms(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(optionalProgramResponseType)).thenReturn(Mono.just(entity));

        List<StudentOptionalProgram> result = gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception);
        assertNotNull(result);
        LOG.debug(">getStudentOptionalProgramsByIdTest");
    }

    @Test
    public void getStudentOptionalProgramsByIdTest_Exception() {
        LOG.debug("<{}.getStudentOptionalProgramsByIdTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        UUID studentID = UUID.randomUUID();
        String accessToken = "accessToken";

        List<StudentOptionalProgram> entity = new ArrayList<>();

        ParameterizedTypeReference<List<StudentOptionalProgram>> optionalProgramResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentOptionalPrograms(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));

        List<StudentOptionalProgram> result = gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception);
        assertNotNull(result);
        LOG.debug(">getStudentOptionalProgramsByIdTest");
    }
}
