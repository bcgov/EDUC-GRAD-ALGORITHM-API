package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GradProgramManagementService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradProgramManagementService.class);

    @Autowired
    private WebClient webClient;

    GradLetterGrades getAllLetterGrades(String accessToken) {
        start();
        GradLetterGrades result = webClient.get()
                .uri("https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/lettergrade")
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
                .uri("https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/"
                        + "programrules?programCode=" + programCode)
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
                .uri("https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/specialcase")
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
                .uri("https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/"
                        + "specialprogramrules/" + gradProgram + "/" + gradSpecialProgram)
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
                .uri("https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/specialprograms/"
                        + gradProgram + "/" + gradSpecialProgram)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradSpecialProgram.class)
                .block();
        end();
        return result != null ? result.getId() : null;
    }
}
