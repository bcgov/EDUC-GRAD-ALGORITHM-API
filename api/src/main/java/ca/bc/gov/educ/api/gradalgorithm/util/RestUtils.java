package ca.bc.gov.educ.api.gradalgorithm.util;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.constants.TopicsEnum;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import ca.bc.gov.educ.api.gradalgorithm.exception.ServiceException;
import ca.bc.gov.educ.api.gradalgorithm.messaging.MessagePublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RestUtils {

  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public RestUtils(MessagePublisher messagePublisher) {
    this.messagePublisher = messagePublisher;
  }

  /**
   * Generic method to send message requests and handle responses
   */
  @Retryable(retryFor = {ServiceException.class, Exception.class}, maxAttempts = 4,
      noRetryFor = {EntityNotFoundException.class},
      backoff = @Backoff(multiplier = 2, delay = 2000))
  public <T> T sendMessageRequest(TopicsEnum topic, EventType eventType, String payload,
                                  TypeReference<T> typeRef, T defaultValue) {
    try {
      UUID correlationID = UUID.randomUUID();
      Event event = Event.builder()
          .sagaId(correlationID)
          .eventType(eventType)
          .eventPayload(payload)
          .build();

      val responseMessage = messagePublisher
          .requestMessage(topic.toString(), objectMapper.writeValueAsBytes(event))
          .completeOnTimeout(null, 120, TimeUnit.SECONDS)
          .get();

      if (responseMessage == null) {
        throw new ServiceException("NATS request timed out for " + eventType + " with correlation ID: " + correlationID);
      }

      byte[] responseData = responseMessage.getData();
      if (responseData == null || responseData.length == 0) {
        if(defaultValue != null) {
          log.debug("Empty response data for {}; returning default value", eventType);
          return defaultValue;
        } else {
          //Value expected, so throw exception
          throw new EntityNotFoundException(typeRef.getType().getTypeName());
        }
      }
      return objectMapper.readValue(responseData, typeRef);
    } catch (EntityNotFoundException ex) {
      log.debug("Entity Not Found occurred calling {} service :: {}", eventType, ex.getMessage());
      throw ex;
    } catch (final Exception ex) {
      log.error("Error occurred calling {} service :: {}", eventType, ex.getMessage());
      throw new ServiceException("Failed to retrieve " + eventType + " data: " + ex.getMessage());
    }
  }
}