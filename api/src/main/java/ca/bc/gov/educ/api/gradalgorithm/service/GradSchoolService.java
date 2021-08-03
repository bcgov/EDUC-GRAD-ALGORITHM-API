package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradSchoolService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradSchoolService.class);

    @Autowired
    private WebClient webClient;
    
    @Autowired
    private GradAlgorithmAPIConstants constants;

    School getSchool(String minCode, String accessToken) {
    	logger.debug("getSchool");
        start();
        School schObj = webClient.get()
                .uri(String.format(constants.getSchoolByMincode(), minCode))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(School.class)
                .block();
        end();
        return schObj;
    }
}
