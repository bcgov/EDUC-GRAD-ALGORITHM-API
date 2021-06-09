package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmRules;

import java.util.List;

@Service
public class GradCommonService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCommonService.class);

    @Autowired
    private WebClient webClient;

    List<GradAlgorithmRules> getGradAlgorithmRules(String gradProgram, String accessToken) {
        start();
        List<GradAlgorithmRules> result = webClient.get()
                .uri("https://educ-grad-common-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/common/algorithm-rules/main/" + gradProgram)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradAlgorithmRules>>(){})
                .block();
        end();

        logger.info("**** # of Grad Algorithm Rules: " + (result != null ? result.size() : 0));

        return result;
    }
}
