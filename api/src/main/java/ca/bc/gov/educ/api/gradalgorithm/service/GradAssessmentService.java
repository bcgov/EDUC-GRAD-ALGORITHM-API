package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@Service
public class GradAssessmentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradAssessmentService.class);
    
    @Autowired WebClient webClient;
    @Autowired GradAlgorithmAPIConstants constants;

	@Retry(name = "generalgetcall")
	Mono<AssessmentAlgorithmData> getAssessmentDataForAlgorithm(String pen,String accessToken, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
		{
			start();
			Mono<AssessmentAlgorithmData> result = webClient.get()
					.uri(String.format(constants.getAssessmentData(),pen))
					.headers(h -> h.setBearerAuth(accessToken))
					.retrieve()
					.bodyToMono(AssessmentAlgorithmData.class);
			end();
			return result;
		} catch (Exception e) {
			exception.setExceptionName("GRAD-ASSESSMENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	AssessmentAlgorithmData prepareAssessmentDataForAlgorithm(AssessmentAlgorithmData result) {
		if(result != null && !result.getStudentAssessments().isEmpty()) {
			for (StudentAssessment studentAssessment : result.getStudentAssessments()) {
				studentAssessment.setGradReqMet("");
				studentAssessment.setGradReqMetDetail("");
			}

			logger.debug("**** # of Student Assessments: {}", result.getStudentAssessments() != null ? result.getStudentAssessments().size() : 0);
			logger.debug("**** # of Assessment Requirements: {}", result.getAssessmentRequirements() != null ? result.getAssessmentRequirements().size() : 0);
			logger.debug("**** # of Assessments: {}", result.getAssessments() != null ? result.getAssessments().size() : 0);
		}
		return result;

	}
}
