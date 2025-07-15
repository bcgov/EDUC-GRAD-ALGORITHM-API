package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ParallelDataFetchTest extends EducGradAlgorithmTestBase {

    @Autowired ParallelDataFetch parallelDataFetch;
    @MockBean GradCourseService gradCourseService;
    @MockBean GradAssessmentService gradAssessmentService;
    @MockBean
    GradProgramService gradProgramService;
    @MockBean
    StudentGraduationService studentGraduationService;
    @MockBean
    GradSchoolService gradSchoolService;

    @MockBean(name = "algorithmApiClient")
    @Qualifier("algorithmApiClient")
    WebClient algorithmApiClient;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    @Autowired
    GradAlgorithmAPIConstants constants;

    @BeforeClass
    public static void setup() {

    }

    @After
    public void tearDown() {

    }

    @Before
    public void init() {
        this.gradProgramService.init();
        openMocks(this);
    }

    @Test
    public void testGetALlAlgDataParallelly() throws Exception {
        CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
        AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
        ExceptionMessage exception = new ExceptionMessage();
        String pen = "1312311231";
        AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);

        Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, exception)).thenReturn(Mono.just(courseAlgorithmData));
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, exception)).thenReturn(Mono.just(assessmentAlgorithmData));
        Mono<AlgorithmDataParallelDTO> data = parallelDataFetch.fetchAlgorithmRequiredData(pen, exception);
        assertNotNull(data.block().assessmentAlgorithmData());
        assertEquals(data.block().assessmentAlgorithmData().getAssessments().size(),parallelDTO.assessmentAlgorithmData().getAssessments().size());
        assertNotNull(data.block().courseAlgorithmData());
        assertEquals(data.block().courseAlgorithmData().getStudentCourses().size(),parallelDTO.courseAlgorithmData().getStudentCourses().size());
    }
}
