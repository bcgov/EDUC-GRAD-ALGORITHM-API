package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventHandlerDelegatorService {

    private final EventHandlerService eventHandlerService;

    @Autowired
    public EventHandlerDelegatorService(EventHandlerService eventHandlerService) {
        this.eventHandlerService = eventHandlerService;
    }

    public void handleEvent(final Event event) {
        try {
            log.trace("Handling event {}, in try block", event.getEventType());
            switch (event.getEventType()) {
                case CREATE_SCHOOL, UPDATE_SCHOOL:
                    debugEventLog(event);
                    this.eventHandlerService.handleSchoolEvent(event);
                    break;
                case MOVE_SCHOOL:
                    debugEventLog(event);
                    this.eventHandlerService.handleMoveSchoolEvent(event);
                    break;
                case UPDATE_GRAD_SCHOOL, CREATE_GRAD_SCHOOL:
                    debugEventLog(event);
                    this.eventHandlerService.handleGradSchoolEvent(event);
                    break;
                default:
                    log.trace("silently ignoring other events :: {}", event);
                    break;
            }
        } catch (final Exception e) {
            log.error("Exception", e);
        }
    }

    private void debugEventLog(Event event) {
        log.debug("Processing {} eventEntity record :: {} ", event.getEventType(), event);
    }
}
