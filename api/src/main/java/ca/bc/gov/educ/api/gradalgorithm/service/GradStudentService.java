package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GradStudentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    GradSearchStudent getStudentDemographicsWithRestTemplate(String pen, String accessToken) {
        HttpHeaders httpHeaders = APIUtils.getHeaders(accessToken);

        logger.debug("GET Grad Student Demographics: " + GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/*****" + pen.substring(5));
        start();
        List<GradSearchStudent> resultList = restTemplate.exchange(
                GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/" + pen, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradSearchStudent>>() {
                }).getBody();
        end();
        GradSearchStudent result = resultList.get(0);
        logger.debug((result != null ? result.getLegalLastName().trim() : null) + ", "
                + (result != null ? result.getLegalFirstName().trim() : null));
        return result;
    }

    GradSearchStudent getStudentDemographics(String pen, String accessToken) {
        logger.debug("GET Grad Student Demographics: " + GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/*****" + pen.substring(5));

        start();
        List<GradSearchStudent> resultList = webClient.get()
                .uri(GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/" + pen)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradSearchStudent>>(){})
                .block();
        end();

        GradSearchStudent result = resultList != null ? resultList.get(0) : null;
        logger.debug((result != null ? result.getLegalLastName().trim() : null) + ", "
                + (result != null ? result.getLegalFirstName().trim() : null));
        return result;
    }
}
