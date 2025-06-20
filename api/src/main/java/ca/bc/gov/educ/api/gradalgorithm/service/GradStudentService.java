package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Slf4j
@Service
public class GradStudentService extends GradService {

	public GradStudentService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	@Retry(name = "generalgetcall")
    GradSearchStudent getStudentDemographics(UUID studentID) {
        start();
        GradSearchStudent result = restService.get(String.format(constants.getStudentDemographics(), studentID),
				GradSearchStudent.class, algorithmApiClient);
        end();

		log.debug("**** # of Student {},{}:",(result != null ? result.getLegalLastName().trim() : null),(result != null ? result.getLegalFirstName().trim() : null));
        return result;
    }


	@Retry(name = "generalgetcall")
	public GradStudentAlgorithmData getGradStudentData(UUID studentID, ExceptionMessage exception) {
		try {
			start();
			GradStudentAlgorithmData result = restService.get(String.format(constants.getGradStudentAlgorithmData(), studentID),
					GradStudentAlgorithmData.class, algorithmApiClient);
	        end();

			if(result != null)
				log.debug("**** # of Student {},{}:",(result.getGradStudent() != null ? result.getGradStudent().getLegalFirstName() : null) , (result.getGradStudent() != null ? result.getGradStudent().getLegalLastName().trim() : null));

			return result;
		} catch (Exception e) {
			exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}        
	}
}
