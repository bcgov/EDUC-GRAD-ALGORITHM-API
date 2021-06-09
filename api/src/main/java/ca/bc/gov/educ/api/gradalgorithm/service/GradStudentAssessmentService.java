package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.struct.StudentAssessment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GradStudentAssessmentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentAssessmentService.class);

    @Autowired
    private WebClient webClient;

    List<StudentAssessment> getAllAssessmentsForAStudent(String pen, String accessToken) {

        start();
        StudentAssessment[] result = webClient.get()
                .uri("https://student-assessment-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/studentassessment/pen"
                        + "/" + pen)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StudentAssessment[]>(){})
                .block()
                ;
        end();

        logger.info("**** # of Assessments: " + (result != null ? result.length : 0));

        for (StudentAssessment studentAssessment : result) {
            studentAssessment.setGradReqMet("");
            studentAssessment.setGradReqMetDetail("");
        }

        return Arrays.asList(result != null ? result.clone() : new StudentAssessment[0]);
    }
}
