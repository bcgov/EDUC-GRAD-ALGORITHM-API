package ca.bc.gov.educ.api.gradalgorithm.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradSchoolService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradSchoolService.class);
    
    @Autowired WebClient webClient;
    @Autowired GradAlgorithmAPIConstants constants;

	@Retry(name = "generalgetcall")
    School getSchool(String minCode, String accessToken,ExceptionMessage exception) {
    	logger.debug("getSchool");
		exception = new ExceptionMessage();
    	try
    	{
	        start();
	        School schObj = webClient.get()
	                .uri(String.format(constants.getSchoolByMincode(), minCode))
	                .headers(h -> h.setBearerAuth(accessToken))
	                .retrieve()
	                .bodyToMono(School.class)
	                .block();
	        end();
	        return schObj;
    	} catch (Exception e) {
    		exception.setExceptionName("GRAD-TRAX-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
    }
}
