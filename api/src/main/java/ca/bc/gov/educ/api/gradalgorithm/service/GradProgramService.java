package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.PROGRAM_MANAGEMENT_BASE_URL;

@Service
public class GradProgramService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradProgramService.class);

    @Autowired
    private WebClient webClient;


    List<GradProgramRule> getProgramRules(String programCode, String accessToken) {
        start();
        List<GradProgramRule> result = webClient.get()
                .uri(PROGRAM_MANAGEMENT_BASE_URL + "/programrules?programCode=" + programCode)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradProgramRule>>(){})
                .block();
        end();
        logger.info("**** # of Program Rules: " + (result != null ? result.size() : 0));

        return result;
    }

    List<GradSpecialProgramRule> getSpecialProgramRules(
            String gradProgram, String gradSpecialProgram, String accessToken) {
        start();
        List<GradSpecialProgramRule> result = webClient.get()
                .uri(PROGRAM_MANAGEMENT_BASE_URL + "/specialprogramrules/" + gradProgram + "/" + gradSpecialProgram)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradSpecialProgramRule>>(){})
                .block();
        end();

        if(result != null)
            logger.info("**** # of Special Program Rules: " + result.size());

        return result;
    }

    UUID getSpecialProgramID(String gradProgram, String gradSpecialProgram, String accessToken) {
        start();
        GradSpecialProgram result = webClient.get()
                .uri(PROGRAM_MANAGEMENT_BASE_URL + "/specialprograms/" + gradProgram + "/" + gradSpecialProgram)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradSpecialProgram.class)
                .block();
        end();
        return result != null ? result.getId() : null;
    }
}
