package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradStudentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private GradAlgorithmAPIConstants constants;


    GradSearchStudent getStudentDemographics(UUID studentID, String accessToken) {
        start();
        GradSearchStudent result = webClient.get()
                .uri(String.format(constants.getStudentDemographics(), studentID))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradSearchStudent.class)
                .block();
        end();

        logger.info("**** # of Student : "+(result != null ? result.getLegalLastName().trim() : null) + ", "
                + (result != null ? result.getLegalFirstName().trim() : null));
        return result;
    }
}
