package ca.bc.gov.educ.api.gradalgorithm.util;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.EventValidation;
import ca.bc.gov.educ.api.gradalgorithm.exception.IgnoreEventException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;


public final class EventUtils {
  private EventUtils() {
  }

  public static Event getEventIfValid(String eventString) throws JsonProcessingException, IgnoreEventException {
    final EventValidation event = JsonTransformer.getJsonObjectFromString(EventValidation.class, eventString);
    if(StringUtils.isNotBlank(event.getEventType()) && !EventType.isValid(event.getEventType())) {
      throw new IgnoreEventException("Invalid event type", event.getEventType(), event.getEventOutcome());
    }
    return JsonTransformer.getJsonObjectFromString(Event.class, eventString);
  }
}
