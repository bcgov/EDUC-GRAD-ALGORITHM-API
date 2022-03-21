package ca.bc.gov.educ.api.gradalgorithm.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@Service
public class StudentGraduationService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(StudentGraduationService.class);
    
    @Autowired WebClient webClient;
    @Autowired GradAlgorithmAPIConstants constants;
    
    private static final String EXCEPTION_MESSAGE = "GRAD-STUDENT-GRADUATION-API IS DOWN";

	@Retry(name = "generalgetcall")
	Mono<StudentGraduationAlgorithmData> getAllAlgorithmData(String programCode,String accessToken, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
		{

			start();
			Mono<StudentGraduationAlgorithmData> result = webClient.get()
					.uri(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata/"+programCode)
					.headers(h -> h.setBearerAuth(accessToken))
					.retrieve()
					.bodyToMono(StudentGraduationAlgorithmData.class);
			end();
			return result;
		} catch (Exception e) {
			exception.setExceptionName(EXCEPTION_MESSAGE);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	@Retry(name = "generalgetcall")
    TranscriptMessage getGradMessages(String gradProgram, String msgType, String accessToken,ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
    	{
        start();
        TranscriptMessage result = webClient.get()
                .uri(String.format(constants.getGraduationMessage(), gradProgram, msgType))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(TranscriptMessage.class)
                .block();
        end();

        return result;
    	} catch (Exception e) {
    		exception.setExceptionName(EXCEPTION_MESSAGE);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
    }
    
}
