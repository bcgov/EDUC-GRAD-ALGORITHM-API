package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.AlgorithmDataParallelDTO;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ParallelDataFetch {
    GradCourseService gradCourseService;
    GradAssessmentService gradAssessmentService;

    @Autowired
    public ParallelDataFetch(GradCourseService gradCourseService, GradAssessmentService gradAssessmentService) {
        this.gradCourseService = gradCourseService;
        this.gradAssessmentService = gradAssessmentService;
    }

    @Retry(name = "generalgetcall")
    public Mono<AlgorithmDataParallelDTO> fetchAlgorithmRequiredData(String pen, ExceptionMessage exception) {
        log.debug("parallel fetchAlgorithmRequiredData");
        Mono<CourseAlgorithmData> courseAlgorithmDataMono = gradCourseService.getCourseDataForAlgorithm(pen, exception);
        Mono<AssessmentAlgorithmData> assessmentAlgorithmDataMono = gradAssessmentService.getAssessmentDataForAlgorithm(pen, exception);
        return Mono.zip(courseAlgorithmDataMono,assessmentAlgorithmDataMono).map(tuple -> new AlgorithmDataParallelDTO(tuple.getT1(),tuple.getT2()));
    }
}
