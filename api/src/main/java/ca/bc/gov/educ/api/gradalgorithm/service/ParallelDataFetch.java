package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.AlgorithmDataParallelDTO;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ParallelDataFetch {
    private static final Logger logger = LoggerFactory.getLogger(ParallelDataFetch.class);

    private GradCourseService gradCourseService;

    private GradAssessmentService gradAssessmentService;


    @Retry(name = "generalgetcall")
    public Mono<AlgorithmDataParallelDTO> fetchAlgorithmRequiredData(String pen,String accessToken, ExceptionMessage exception) {
        logger.debug("parallel fetchAlgorithmRequiredData");
        Mono<CourseAlgorithmData> courseAlgorithmDataMono = gradCourseService.getCourseDataForAlgorithm(pen,accessToken,exception);
        Mono<AssessmentAlgorithmData> assessmentAlgorithmDataMono = gradAssessmentService.getAssessmentDataForAlgorithm(pen,accessToken,exception);
        return Mono.zip(courseAlgorithmDataMono,assessmentAlgorithmDataMono).map(tuple -> new AlgorithmDataParallelDTO(tuple.getT1(),tuple.getT2()));
    }
}
