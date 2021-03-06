package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class GradService {

    private Instant start;
    private Instant end;
    private Duration timeElapsed;

    private static final Logger logger = LoggerFactory.getLogger(GradService.class);

    void start() {
        start = Instant.now();
    }

    void end() {
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        logger.info("Time taken: " + timeElapsed.toMillis() + " milliseconds");
    }
}
