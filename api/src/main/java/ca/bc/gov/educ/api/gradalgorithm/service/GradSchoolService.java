package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.School;

@Service
public class GradSchoolService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradSchoolService.class);

    @Autowired
    private WebClient webClient;

    School getSchool(String minCode, String accessToken) {

        start();
        School schObj = webClient.get()
                .uri("https://educ-grad-school-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/school" + "/"
                        + minCode)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(School.class)
                .block();
        end();
        return schObj;
    }
}
