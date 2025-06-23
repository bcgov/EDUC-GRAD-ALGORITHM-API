package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.function.Consumer;

import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAssessmentServiceTest extends EducGradAlgorithmTestBase {

    @Autowired GradAssessmentService gradAssessmentService;
    @MockBean GradProgramService gradProgramService;

    @Autowired ExceptionMessage exception;

    @MockBean(name = "algorithmApiClient")
    @Qualifier("algorithmApiClient")
    WebClient algorithmApiClient;
    @MockBean GradSchoolService gradSchoolService;
    @MockBean StudentGraduationService studentGraduationService;
    
    @Autowired GradAlgorithmAPIConstants constants;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.ResponseSpec responseMock;

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
    public void testGetAssessmentDataForAlgorithm() throws Exception {
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
        String pen = "1312311231";
        when(this.algorithmApiClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getAssessmentData(),pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(AssessmentAlgorithmData.class)).thenReturn(Mono.just(assessmentAlgorithmData));
        
        Mono<AssessmentAlgorithmData> res = gradAssessmentService.getAssessmentDataForAlgorithm(pen, exception);
        assertNotNull(res.block());
    }

    @Test
    public void testGetAssessmentDataForAlgorithm_throwsException() {
        String pen = "1312311231";
        when(this.algorithmApiClient.get()).thenThrow(new RuntimeException(""));

        Mono<AssessmentAlgorithmData> res = gradAssessmentService.getAssessmentDataForAlgorithm(pen, exception);
        assertNull(res);
    }

    @Test
    public void testprepareAssessmentDataForAlgorithm() throws Exception {
        AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
        AssessmentAlgorithmData res = gradAssessmentService.prepareAssessmentDataForAlgorithm(assessmentAlgorithmData);
        assertNotNull(res);
    }
}
