package ca.bc.gov.educ.api.gradalgorithm.messaging;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import ca.bc.gov.educ.api.gradalgorithm.service.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ca.bc.gov.educ.api.gradalgorithm.util.LogHelper;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

import java.time.Duration;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class SchoolSubscriber {

    private final Connection connection;
    private final GradAlgorithmAPIConstants constants;
    private final Executor messageProcessingThreads;
    private final EventHandlerDelegatorService eventHandlerDelegatorService;


    @Autowired
    public SchoolSubscriber(Connection connection, GradAlgorithmAPIConstants constants, EventHandlerDelegatorService eventHandlerDelegatorService) {
        this.connection = connection;
        this.constants = constants;
        this.eventHandlerDelegatorService = eventHandlerDelegatorService;
        messageProcessingThreads = new EnhancedQueueExecutor.Builder().setThreadFactory(new ThreadFactoryBuilder().setNameFormat("nats-message-subscriber-%d").build()).setCorePoolSize(10).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
    }

    @PostConstruct
    public void subscribe() {
        var dispatcher = connection.createDispatcher(onMessage());
        dispatcher.subscribe("INSTITUTE_EVENTS_TOPIC");
        dispatcher.subscribe("GRAD_SCHOOL_EVENTS_TOPIC");
    }

    private MessageHandler onMessage() {
        return (Message message) -> {
            if (message != null) {
                try {
                    var eventString = new String(message.getData());
                    LogHelper.logMessage(eventString, constants.isSplunkLogHelperEnabled());
                    var event = JsonTransformer.getJsonObjectFromString(Event.class, eventString);
                    log.debug("message sub handling: {}, {}", event, message);
                    messageProcessingThreads.execute(() -> eventHandlerDelegatorService.handleEvent(event));
                } catch (final Exception e) {
                    log.debug("on message error: {}", e.getMessage());
                    log.error("Exception ", e);
                }
            }
        };
    }
}
