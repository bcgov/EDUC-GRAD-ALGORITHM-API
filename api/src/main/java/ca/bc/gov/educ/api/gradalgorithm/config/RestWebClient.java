package ca.bc.gov.educ.api.gradalgorithm.config;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import ca.bc.gov.educ.api.gradalgorithm.util.LogHelper;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.*;
import reactor.netty.http.client.HttpClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
@Profile("!test")
public class RestWebClient {

    @Autowired
    GradAlgorithmAPIConstants constants;

    private final HttpClient httpClient;

    public RestWebClient() {
        this.httpClient = HttpClient.create().compress(true)
                .resolver(spec -> spec.queryTimeout(Duration.ofMillis(200)).trace("DNS", LogLevel.TRACE));
        this.httpClient.warmup().block();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))  // 10MB
                    .build())
                .filter(setRequestHeaders())
                .filter(this.log())
                .build();
    }

    private ExchangeFilterFunction log() {
        return (clientRequest, next) -> next
                .exchange(clientRequest)
                .doOnNext((clientResponse -> LogHelper.logClientHttpReqResponseDetails(
                        clientRequest.method(),
                        clientRequest.url().toString(),
                        //Grad2-1929 Refactoring/Linting replaced rawStatusCode() with statusCode() as it was deprecated
                        clientResponse.statusCode().value(),
                        clientRequest.headers().get(GradAlgorithmAPIConstants.CORRELATION_ID),
                        clientRequest.headers().get(GradAlgorithmAPIConstants.REQUEST_SOURCE),
                        constants.isSplunkLogHelperEnabled())
                ));
    }

    private ExchangeFilterFunction setRequestHeaders() {
        return (clientRequest, next) -> {
            ClientRequest modifiedRequest = ClientRequest.from(clientRequest)
                    .header(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID())
                    .header(GradAlgorithmAPIConstants.USER_NAME, ThreadLocalStateUtil.getCurrentUser())
                    .header(GradAlgorithmAPIConstants.REQUEST_SOURCE, GradAlgorithmAPIConstants.API_NAME)
                    .build();
            return next.exchange(modifiedRequest);
        };
    }
}
