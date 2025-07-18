package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GradGraduationStatusService extends GradService {

	@Autowired
	public GradGraduationStatusService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	@Retry(name = "generalgetcall")
    GradAlgorithmGraduationStudentRecord getStudentGraduationStatus(String studentID) {
        
    	start();
        GradAlgorithmGraduationStudentRecord result = restService.get(String.format(constants.getGraduationStudentRecord(),studentID),
				GradAlgorithmGraduationStudentRecord.class, algorithmApiClient);
        end();

		if(result != null)
			log.debug("**** # of Graduation Record : {}",result.getStudentID());

        return result;
    }

	@Retry(name = "generalgetcall")
    List<StudentOptionalProgram> getStudentOptionalProgramsById(String studentID, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
    	{
	        start();
	        List<StudentOptionalProgram> result = restService.get(String.format(constants.getStudentOptionalPrograms(), studentID),
					new ParameterizedTypeReference<List<StudentOptionalProgram>>(){}, algorithmApiClient);
	        end();

			log.debug("**** # of Optional Programs: {}",result != null ? result.size() : 0);
	        return result == null ? new ArrayList<>():result;
    	} catch (Exception e) {
    		exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
    		return new ArrayList<>();
		}
    }
}
