package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GradRuleProcessorService extends GradService {

	@Autowired
	public GradRuleProcessorService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	@Retry(name = "generalgetcall")
    RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData, ExceptionMessage exception) {
		log.debug("**** Processing Grad Algorithm Rules");
        try
        {
        	start();
	        RuleProcessorData result = restService.post(constants.getRunRules(), ruleProcessorData,
					RuleProcessorData.class, algorithmApiClient);
	        end();
	        return result;
        } catch (Exception e) {
        	exception.setExceptionName("RULE-ENGINE-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
    }
}
