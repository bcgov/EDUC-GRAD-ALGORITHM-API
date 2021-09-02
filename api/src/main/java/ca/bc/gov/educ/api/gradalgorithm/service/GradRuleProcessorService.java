package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.exception.GradBusinessRuleException;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradRuleProcessorService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradRuleProcessorService.class);

    @Autowired
    private WebClient webClient;
    
    @Autowired
    private GradAlgorithmAPIConstants constants;

    RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData, String accessToken) {
        logger.info("**** Processing Grad Algorithm Rules");
        try
        {
        	start();
	        RuleProcessorData result = webClient.post()
	                .uri(constants.getRuleEngineBaseURL() + "/" + constants.getRunRules())
	                .headers(h -> h.setBearerAuth(accessToken))
	                .body(BodyInserters.fromValue(ruleProcessorData))
	                .retrieve()
	                .bodyToMono(RuleProcessorData.class)
	                .block();
	        end();
	        return result;
        } catch (Exception e) {
			throw new GradBusinessRuleException("RULE-ENGINE-API IS DOWN");
		}
    }
}
