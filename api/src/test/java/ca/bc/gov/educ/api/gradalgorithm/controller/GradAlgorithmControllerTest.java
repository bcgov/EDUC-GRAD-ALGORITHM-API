package ca.bc.gov.educ.api.gradalgorithm.controller;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.service.GradAlgorithmService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class GradAlgorithmControllerTest extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradAlgorithmControllerTest.class);
    private static final String CLASS_NAME = GradAlgorithmControllerTest.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Mock
    GradAlgorithmService gradAlgorithmService;

    @MockBean
    WebClient webClient;

    @MockBean
    GradProgramService gradProgramService;
    @MockBean
    GradSchoolService gradSchoolService;
    @MockBean
    StudentGraduationService studentGraduationService;

    @InjectMocks
    private GradAlgorithmController gradAlgorithmController;


    @BeforeEach
    public void init() {

        this.gradProgramService.init();
        this.gradSchoolService.init();
        this.studentGraduationService.init();
        openMocks(this);
    }

    @Test
    void graduateStudentTest() throws Exception {
        LOG.debug("<{}.graduateStudentTest at {}", CLASS_NAME, dateFormat.format(new Date()));

        RuleProcessorData ruleProcessorData = createRuleProcessorData("json/ruleProcessorData.json");

        assertNotNull(ruleProcessorData);
        assertNotNull(ruleProcessorData.getGradStudent());

        String studentID = ruleProcessorData.getGradStudent().getStudentID();
        String gradProgram = ruleProcessorData.getGradStudent().getProgram();

        GraduationData entity = new GraduationData();
        entity.setGradStudent(ruleProcessorData.getGradStudent());
        entity.setGradStatus(ruleProcessorData.getGradStatus());
        entity.setSchool(ruleProcessorData.getSchool());


        Mockito.when(gradAlgorithmService.graduateStudent(UUID.fromString(studentID), gradProgram, false, null, "accessToken")).thenReturn(entity);
        gradAlgorithmController.graduateStudent(studentID, gradProgram, false, null, "accessToken");
        Mockito.verify(gradAlgorithmService).graduateStudent(UUID.fromString(studentID), gradProgram, false, null, "accessToken");

        LOG.debug(">graduateStudentTest");
    }

}
