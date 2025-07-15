package ca.bc.gov.educ.api.gradalgorithm.service;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GradAssessmentService extends GradService {

	public GradAssessmentService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	@Retry(name = "generalgetcall")
	Mono<AssessmentAlgorithmData> getAssessmentDataForAlgorithm(String pen, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
		{
			start();
			AssessmentAlgorithmData result = restService.get(String.format(constants.getAssessmentData(),pen),
					AssessmentAlgorithmData.class, algorithmApiClient);
			end();
			return Mono.just(result);
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

			log.debug("**** # of Student Assessments: {}", result.getStudentAssessments() != null ? result.getStudentAssessments().size() : 0);
			log.debug("**** # of Assessment Requirements: {}", result.getAssessmentRequirements() != null ? result.getAssessmentRequirements().size() : 0);
			log.debug("**** # of Assessments: {}", result.getAssessments() != null ? result.getAssessments().size() : 0);
		}
		return result;

	}
}
