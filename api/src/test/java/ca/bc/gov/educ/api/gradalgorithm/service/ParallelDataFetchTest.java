package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ParallelDataFetchTest extends EducGradAlgorithmTestBase {

    @Autowired ParallelDataFetch parallelDataFetch;
    @MockBean GradCourseService gradCourseService;
    @MockBean GradAssessmentService gradAssessmentService;
    @MockBean StudentGraduationService studentGraduationService;
    @MockBean GradSchoolService gradSchoolService;

    @MockBean WebClient webClient;

    @BeforeClass
    public static void setup() {

    }

    @After
    public void tearDown() {

    }

    @Before
    public void init() {
        openMocks(this);
    }

    @Test
    public void testGetALlAlgDataParallelly() throws Exception {
        CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
        AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
        StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
        School school = createSchoolData("json/school.json");
        String accessToken = "accessToken";
        ExceptionMessage exception = new ExceptionMessage();
        String pen = "1312311231";
        String gradProgram = "2018-EN";
        String schoolOfRecord = "32343242";
        AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);

        Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(Mono.just(courseAlgorithmData));
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(Mono.just(assessmentAlgorithmData));

        Mono<AlgorithmDataParallelDTO> data = parallelDataFetch.fetchAlgorithmRequiredData(gradProgram,pen,schoolOfRecord,accessToken,exception);
        assertNotNull(data.block().assessmentAlgorithmData());
        assertEquals(data.block().assessmentAlgorithmData().getAssessments().size(),parallelDTO.assessmentAlgorithmData().getAssessments().size());
    }
}
