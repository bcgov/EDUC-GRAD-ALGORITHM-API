package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
@AllArgsConstructor
public class GradGraduationStatusService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradGraduationStatusService.class);

	private WebClient webClient;
	private GradAlgorithmAPIConstants constants;

	@Retry(name = "generalgetcall")
    GradAlgorithmGraduationStudentRecord getStudentGraduationStatus(String studentID,String accessToken) {
        
    	start();
        GradAlgorithmGraduationStudentRecord result = webClient.get()
                .uri(String.format(constants.getGraduationStudentRecord(),studentID))
                .headers(h -> {
									h.setBearerAuth(accessToken);
									h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
								})
                .retrieve()
                .bodyToMono(GradAlgorithmGraduationStudentRecord.class)
                .block();
        end();

		if(result != null)
        	logger.debug("**** # of Graduation Record : {}",result.getStudentID());

        return result;
    }

	@Retry(name = "generalgetcall")
    List<StudentOptionalProgram> getStudentOptionalProgramsById(String studentID, String accessToken,ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
    	{
	        start();
	        List<StudentOptionalProgram> result = webClient.get()
	                .uri(String.format(constants.getStudentOptionalPrograms(), studentID))
	                .headers(h -> {
										h.setBearerAuth(accessToken);
										h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
									})
	                .retrieve()
	                .bodyToMono(new ParameterizedTypeReference<List<StudentOptionalProgram>>(){})
	                .block();
	        end();
	
	        logger.debug("**** # of Optional Programs: {}",result != null ? result.size() : 0);
	        return result == null ? new ArrayList<>():result;
    	} catch (Exception e) {
    		exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
    		return new ArrayList<>();
		}
    }
}
