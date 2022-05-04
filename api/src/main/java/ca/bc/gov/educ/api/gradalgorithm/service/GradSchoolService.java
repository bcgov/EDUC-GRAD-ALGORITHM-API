package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@Service
public class GradSchoolService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradSchoolService.class);
    
    @Autowired WebClient webClient;
    @Autowired GradAlgorithmAPIConstants constants;

	@Retry(name = "generalgetcall")
	Mono<School> getSchool(String minCode, String accessToken,ExceptionMessage exception) {
    	logger.debug("getSchool");
    	try
    	{
	        start();
	        Mono<School> schObj = webClient.get()
	                .uri(String.format(constants.getSchoolByMincode(), minCode))
	                .headers(h -> {
										h.setBearerAuth(accessToken);
										h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
									})
	                .retrieve()
	                .bodyToMono(School.class);
	        end();
	        return schObj;
    	} catch (Exception e) {
    		exception.setExceptionName("GRAD-TRAX-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
    }
}
