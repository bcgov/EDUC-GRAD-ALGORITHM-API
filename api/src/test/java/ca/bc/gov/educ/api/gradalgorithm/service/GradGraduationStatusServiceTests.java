package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradGraduationStatusServiceTests {

    private static final String CLASS_NAME = GradGraduationStatusServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired GradGraduationStatusService gradGraduationStatusService;
    @Autowired ExceptionMessage exception;
    @MockBean(name = "algorithmApiClient")
    @Qualifier("algorithmApiClient")
    WebClient algorithmApiClient;
    @MockBean GradProgramService gradProgramService;
    @MockBean GradSchoolService gradSchoolService;
    @MockBean StudentGraduationService studentGraduationService;
    @MockBean RESTService restServiceMock;
    @Autowired GradAlgorithmAPIConstants constants;

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
        log.debug("<{}.getStudentOptionalProgramsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "1111111111";
        UUID studentID = UUID.randomUUID();

        GradAlgorithmGraduationStudentRecord entity = new GradAlgorithmGraduationStudentRecord();
        entity.setPen(pen);
        entity.setStudentID(studentID);

        when(this.restServiceMock.get(String.format(constants.getGraduationStudentRecord(), studentID), GradAlgorithmGraduationStudentRecord.class,
                algorithmApiClient)).thenReturn(entity);

        GradAlgorithmGraduationStudentRecord result = gradGraduationStatusService.getStudentGraduationStatus(studentID.toString());
        assertNotNull(result);
        log.debug(">getStudentOptionalProgramsTest");
    }

    @Test
    public void getStudentGraduationStatusTest_returnNullResponse() {
        log.debug("<{}.getStudentOptionalProgramsTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String pen = "1111111111";
        UUID studentID = UUID.randomUUID();

        GradAlgorithmGraduationStudentRecord entity = new GradAlgorithmGraduationStudentRecord();
        entity.setPen(pen);
        entity.setStudentID(studentID);

        when(this.restServiceMock.get(String.format(constants.getGraduationStudentRecord(), studentID), GradAlgorithmGraduationStudentRecord.class,
                algorithmApiClient)).thenReturn(null);

        GradAlgorithmGraduationStudentRecord result = gradGraduationStatusService.getStudentGraduationStatus(studentID.toString());
        assertNull(result);
        log.debug(">getStudentGraduationStatusTest_returnNullResponse");
    }

    @Test
    public void getStudentOptionalProgramsByIdTest() {
        log.debug("<{}.getStudentOptionalProgramsByIdTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        UUID studentID = UUID.randomUUID();
        List<StudentOptionalProgram> entity = new ArrayList<>();
        ParameterizedTypeReference<List<StudentOptionalProgram>> optionalProgramResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.restServiceMock.get(String.format(constants.getStudentOptionalPrograms(), studentID), optionalProgramResponseType,
                algorithmApiClient)).thenReturn(entity);

        List<StudentOptionalProgram> result = gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), exception);
        assertNotNull(result);
        log.debug(">getStudentOptionalProgramsByIdTest");
    }

    @Test
    public void getStudentOptionalProgramsByIdTest_Exception() {
        log.debug("<{}.getStudentOptionalProgramsByIdTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        UUID studentID = UUID.randomUUID();
        ParameterizedTypeReference<List<StudentOptionalProgram>> optionalProgramResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.restServiceMock.get(String.format(constants.getStudentOptionalPrograms(), studentID), optionalProgramResponseType,
                algorithmApiClient)).thenThrow(new RuntimeException());

        List<StudentOptionalProgram> result = gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), exception);
        assertNotNull(result);
        log.debug(">getStudentOptionalProgramsByIdTest");
    }
}
