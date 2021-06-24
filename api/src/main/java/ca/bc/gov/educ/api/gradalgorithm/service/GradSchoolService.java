package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GET_SCHOOL_BY_MINCODE;

@Service
public class GradSchoolService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradSchoolService.class);

    @Autowired
    private WebClient webClient;

    School getSchool(String minCode, String accessToken) {

        start();
        School schObj = webClient.get()
                .uri(String.format(GET_SCHOOL_BY_MINCODE, minCode))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(School.class)
                .block();
        end();
        return schObj;
    }
}
