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
public class GradProgramManagementService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradProgramManagementService.class);

    @Autowired
    private WebClient webClient;

    GradLetterGrades getAllLetterGrades(String accessToken) {
        start();
        GradLetterGrades result = webClient.get()
                .uri(PROGRAM_MANAGEMENT_BASE_URL + "/lettergrade")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradLetterGrades.class)
                .block();
        end();

        logger.info("**** # of Letter Grades: " + (result != null ? result.getGradLetterGradeList().size() : 0));
        return result;
    }

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

    List<GradSpecialCase> getAllSpecialCases(String accessToken) {
        start();
        List<GradSpecialCase> result = webClient.get()
                .uri(PROGRAM_MANAGEMENT_BASE_URL + "/specialcase")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradSpecialCase>>(){})
                .block();
        end();

        logger.info("**** # of Special Cases: " + (result != null ? result.size() : 0));

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
