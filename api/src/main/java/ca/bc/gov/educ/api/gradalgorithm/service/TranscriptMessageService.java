package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class TranscriptMessageService extends GradService {

    private static final String EXCEPTION_MESSAGE = "GRAD-STUDENT-GRADUATION-API IS DOWN";

	@Retry(name = "generalgetcall")
    TranscriptMessage getGradMessages(String gradProgram, String msgType, String accessToken,ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
    	{
        start();
        TranscriptMessage result = webClient.get()
                .uri(String.format(constants.getGraduationMessage(), gradProgram, msgType))
                .headers(h -> {
									h.setBearerAuth(accessToken);
									h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
								})
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
