package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GET_STUDENT_ASSESSMENT_BY_PEN;

@Service
public class GradStudentAssessmentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentAssessmentService.class);

    @Autowired
    private WebClient webClient;

    List<StudentAssessment> getAllAssessmentsForAStudent(String pen, String accessToken) {

        start();
        StudentAssessment[] result = webClient.get()
                .uri(GET_STUDENT_ASSESSMENT_BY_PEN + "/" + pen)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StudentAssessment[]>(){})
                .block()
                ;
        end();

        logger.info("**** # of Assessments: " + (result != null ? result.length : 0));
        if(result != null) {
	        for (StudentAssessment studentAssessment : result) {
	            studentAssessment.setGradReqMet("");
	            studentAssessment.setGradReqMetDetail("");
	        }
        }

        return Arrays.asList(result != null ? result.clone() : new StudentAssessment[0]);
    }
}
