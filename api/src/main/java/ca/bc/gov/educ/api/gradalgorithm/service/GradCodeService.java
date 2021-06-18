package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradMessaging;

@Service
public class GradCodeService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCommonService.class);

    @Autowired
    private WebClient webClient;
    GradMessaging getGradMessages(String gradProgram, String msgType, String accessToken) {

        start();
        GradMessaging result = webClient.get()
                .uri(String.format("https://educ-grad-code-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/code/gradmessages/pgmCode/%s/msgType/%s",
                        gradProgram,msgType))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradMessaging.class)
                .block();
        end();

        return result;
    }
}
