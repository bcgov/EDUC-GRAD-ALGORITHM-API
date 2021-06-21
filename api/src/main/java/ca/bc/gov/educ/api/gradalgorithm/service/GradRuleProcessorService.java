package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GradRuleProcessorService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradRuleProcessorService.class);

    @Autowired
    private WebClient webClient;

    RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData, String accessToken) {
        logger.info("**** Processing Grad Algorithm Rules");

        start();

        RuleProcessorData result = webClient.post()
                .uri(GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
                        + GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_RUN_GRAD_ALGORITHM_RULES)
                .headers(h -> h.setBearerAuth(accessToken))
                .body(BodyInserters.fromValue(ruleProcessorData))
                .retrieve()
                .bodyToMono(RuleProcessorData.class)
                .block();
        end();
        return result;
    }
}
