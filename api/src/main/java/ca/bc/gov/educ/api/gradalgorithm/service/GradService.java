package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ResponseObj;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;

@Slf4j
public class GradService {

    private Instant start;
    protected GradAlgorithmAPIConstants constants;
    protected WebClient algorithmApiClient;
    protected RESTService restService;

    @Autowired
    public GradService(GradAlgorithmAPIConstants constants, @Qualifier("algorithmApiClient") WebClient algorithmApiClient,
                       RESTService restService) {
        this.constants = constants;
        this.algorithmApiClient = algorithmApiClient;
        this.restService = restService;
    }

    protected void start() {
        start = Instant.now();
    }

    protected void end() {
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        log.debug("Time taken: {} milliseconds",timeElapsed.toMillis());
    }

    public ResponseObj getTokenResponseObject() {
        HttpHeaders httpHeadersKC = APIUtils.getHeaders(
                constants.getUserName(), constants.getPassword());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return this.algorithmApiClient.post().uri(constants.getTokenUrl())
                .headers(h -> h.addAll(httpHeadersKC))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .bodyToMono(ResponseObj.class).block();
    }
}
