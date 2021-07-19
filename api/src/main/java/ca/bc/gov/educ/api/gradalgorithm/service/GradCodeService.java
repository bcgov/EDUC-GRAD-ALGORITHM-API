package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_CODE_GRAD_MESSAGES_PROGRAM_CODE_URL;

@Service
public class GradCodeService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCodeService.class);

    @Autowired
    private WebClient webClient;
    GradMessaging getGradMessages(String gradProgram, String msgType, String accessToken) {

        start();
        GradMessaging result = webClient.get()
                .uri(String.format(GRAD_CODE_GRAD_MESSAGES_PROGRAM_CODE_URL, gradProgram, msgType))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradMessaging.class)
                .block();
        end();

        return result;
    }
}
