package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ParallelDataFetch {
    private static final Logger logger = LoggerFactory.getLogger(ParallelDataFetch.class);

    @Autowired
    StudentGraduationService studentGraduationService;

    @Autowired
    GradCourseService gradCourseService;

    @Autowired
    GradAssessmentService gradAssessmentService;

    @Autowired
    GradSchoolService gradSchoolService;

    @Retry(name = "generalgetcall")
    public Mono<AlgorithmDataParallelDTO> fetchAlgorithmRequiredData(String gradProgram,String pen,String schoolOfRecord, String accessToken, ExceptionMessage exception) {
        logger.debug("parallel fetchAlgorithmRequiredData");
        Mono<CourseAlgorithmData> courseAlgorithmDataMono = gradCourseService.getCourseDataForAlgorithm(pen,accessToken,exception);
        Mono<AssessmentAlgorithmData> assessmentAlgorithmDataMono = gradAssessmentService.getAssessmentDataForAlgorithm(pen,accessToken,exception);
        Mono<StudentGraduationAlgorithmData> studentGraduationAlgorithmDataMono = studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception);
        Mono<School> schoolDataMono = gradSchoolService.getSchool(schoolOfRecord, accessToken,exception);
        return Mono.zip(courseAlgorithmDataMono,assessmentAlgorithmDataMono,studentGraduationAlgorithmDataMono,schoolDataMono).map(tuple -> new AlgorithmDataParallelDTO(tuple.getT1(),tuple.getT2(),tuple.getT3(),tuple.getT4()));
    }
}
