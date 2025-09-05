package ca.bc.gov.educ.api.gradalgorithm.service.v2;

import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.AlgorithmDataParallelDTO;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service("parallelDataFetchV2")
public class ParallelDataFetch {
    private final GradCourseService gradCourseService;
    private final GradAssessmentService gradAssessmentService;

    @Autowired
    public ParallelDataFetch(GradCourseService gradCourseService, GradAssessmentService gradAssessmentService) {
        this.gradCourseService = gradCourseService;
      this.gradAssessmentService = gradAssessmentService;
    }

    public Mono<AlgorithmDataParallelDTO> fetchAlgorithmRequiredData(UUID studentID, ExceptionMessage exception) {
        log.debug("parallel fetchAlgorithmRequiredData for studentID: {}", studentID);

        Mono<CourseAlgorithmData> courseAlgorithmDataMono = gradCourseService.getCourseDataForAlgorithm(studentID, exception);
        Mono<AssessmentAlgorithmData> assessmentAlgorithmDataMono = gradAssessmentService.getAssessmentDataForAlgorithm(studentID, exception);

        return Mono.zip(courseAlgorithmDataMono, assessmentAlgorithmDataMono)
            .map(tuple -> new AlgorithmDataParallelDTO(tuple.getT1(), tuple.getT2()));
    }
}
