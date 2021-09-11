package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradGraduationStatusService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradGraduationStatusService.class);
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private GradAlgorithmAPIConstants constants;

    GradAlgorithmGraduationStudentRecord getStudentGraduationStatus(String studentID,String accessToken) {
        
    	start();
        GradAlgorithmGraduationStudentRecord result = webClient.get()
                .uri(String.format(constants.getGraduationStudentRecord(),studentID))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradAlgorithmGraduationStudentRecord.class)
                .block();
        end();

        logger.info("**** # of Graduation Record : " + result.getStudentID());

        return result;
    }

    List<StudentOptionalProgram> getStudentSpecialProgramsById(String studentID, String accessToken,ExceptionMessage exception) {
    	try
    	{
	        start();
	        List<StudentOptionalProgram> result = webClient.get()
	                .uri(String.format(constants.getStudentOptionalPrograms(), studentID))
	                .headers(h -> h.setBearerAuth(accessToken))
	                .retrieve()
	                .bodyToMono(new ParameterizedTypeReference<List<StudentOptionalProgram>>(){})
	                .block();
	        end();
	
	        logger.info("**** # of Special Programs: " + (result != null ? result.size() : 0));
	        return result == null ? new ArrayList<>():result;
    	} catch (Exception e) {
    		exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
    		return new ArrayList<>();
		}
    }
}
