package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ResponseObj;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;

public class GradService {

    private Instant start;
    private static final Logger logger = LoggerFactory.getLogger(GradService.class);

    @Autowired
    GradAlgorithmAPIConstants constants;

    @Autowired
    WebClient webClient;

    void start() {
        start = Instant.now();
    }

    void end() {
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        logger.info("Time taken: {} milliseconds",timeElapsed.toMillis());
    }

    public ResponseObj getTokenResponseObject() {
        HttpHeaders httpHeadersKC = APIUtils.getHeaders(
                constants.getUserName(), constants.getPassword());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return this.webClient.post().uri(constants.getTokenUrl())
                .headers(h -> h.addAll(httpHeadersKC))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .bodyToMono(ResponseObj.class).block();
    }
}
