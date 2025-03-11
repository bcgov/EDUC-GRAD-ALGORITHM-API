package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradRuleProcessorService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradRuleProcessorService.class);
    
    @Autowired WebClient webClient;
    @Autowired GradAlgorithmAPIConstants constants;

	@Retry(name = "generalgetcall")
    RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData, String accessToken,ExceptionMessage exception) {
        logger.debug("**** Processing Grad Algorithm Rules");
        try
        {
        	start();
	        RuleProcessorData result = webClient.post()
	                .uri(constants.getRunRules())
	                .headers(h -> h.setBearerAuth(accessToken))
	                .body(BodyInserters.fromValue(ruleProcessorData))
	                .retrieve()
	                .bodyToMono(RuleProcessorData.class)
	                .block();
	        end();
	        return result;
        } catch (Exception e) {
        	exception.setExceptionName("RULE-ENGINE-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
    }
}
