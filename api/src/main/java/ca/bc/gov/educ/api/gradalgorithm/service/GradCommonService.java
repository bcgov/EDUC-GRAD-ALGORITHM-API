package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_ALGORITHM_RULES_MAIN_GRAD_PROGRAM_URL;

@Service
public class GradCommonService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCommonService.class);

    @Autowired
    private WebClient webClient;

    List<GradAlgorithmRules> getGradAlgorithmRules(String gradProgram, String accessToken) {
        start();
        List<GradAlgorithmRules> result = webClient.get()
                .uri(String.format(GRAD_ALGORITHM_RULES_MAIN_GRAD_PROGRAM_URL, gradProgram))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradAlgorithmRules>>(){})
                .block();
        end();

        logger.info("**** # of Grad Algorithm Rules: " + (result != null ? result.size() : 0));

        return result;
    }
}
