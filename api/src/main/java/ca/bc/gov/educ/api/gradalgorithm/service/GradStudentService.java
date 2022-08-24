package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GradStudentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentService.class);

	@Retry(name = "generalgetcall")
    GradSearchStudent getStudentDemographics(UUID studentID, String accessToken) {
        start();
        GradSearchStudent result = webClient.get()
                .uri(String.format(constants.getStudentDemographics(), studentID))
                .headers(h -> {
									h.setBearerAuth(accessToken);
									h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
								})
                .retrieve()
                .bodyToMono(GradSearchStudent.class)
                .block();
        end();

        logger.info("**** # of Student {},{}:",(result != null ? result.getLegalLastName().trim() : null),(result != null ? result.getLegalFirstName().trim() : null));
        return result;
    }


	@Retry(name = "generalgetcall")
	public GradStudentAlgorithmData getGradStudentData(UUID studentID,String accessToken, ExceptionMessage exception) {
		try {
			start();
			GradStudentAlgorithmData result = webClient.get()
	                .uri(String.format(constants.getGradStudentAlgorithmData(), studentID))
	                .headers(h -> {
										h.setBearerAuth(accessToken);
										h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
									})
	                .retrieve()
	                .bodyToMono(GradStudentAlgorithmData.class)
	                .block();
	        end();

			if(result != null)
	        	logger.info("**** # of Student {},{}:",(result.getGradStudent() != null ? result.getGradStudent().getLegalFirstName() : null) , (result.getGradStudent() != null ? result.getGradStudent().getLegalLastName().trim() : null));

			return result;
		} catch (Exception e) {
			exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}        
	}
}
