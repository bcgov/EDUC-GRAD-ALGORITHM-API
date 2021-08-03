package ca.bc.gov.educ.api.gradalgorithm.controller;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.service.GradAlgorithmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.ActiveProfiles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAlgorithmControllerTest extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradAlgorithmControllerTest.class);
    private static final String CLASS_NAME = GradAlgorithmControllerTest.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Mock
    GradAlgorithmService gradAlgorithmService;

    @InjectMocks
    private GradAlgorithmController gradAlgorithmController;

    @Test
    public void graduateStudentTest() throws Exception {
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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(gradAlgorithmService.graduateStudent(UUID.fromString(studentID), gradProgram, false, null)).thenReturn(entity);
        gradAlgorithmController.graduateStudent(studentID, gradProgram, false);
        Mockito.verify(gradAlgorithmService).graduateStudent(UUID.fromString(studentID), gradProgram, false, null);

        LOG.debug(">graduateStudentTest");
    }

}
