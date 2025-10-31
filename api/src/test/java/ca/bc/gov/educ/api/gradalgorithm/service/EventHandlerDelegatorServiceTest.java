package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventHandlerDelegatorServiceTest {

    @Mock
    EventHandlerService handler;
    @InjectMocks
    EventHandlerDelegatorService delegator;

    private Event createEventWithType(EventType type) {
        Event e = mock(Event.class);
        when(e.getEventType()).thenReturn(type);
        return e;
    }

    @Test
    void createSchool_givenCreateSchoolEventType_delegatesToHandleSchoolEvent() throws Exception {
        var e = createEventWithType(EventType.CREATE_SCHOOL);
        delegator.handleEvent(e);
        verify(handler).handleSchoolEvent(e);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void updateSchool_givenUpdateSchoolEventType_delegatesToHandleSchoolEvent() throws Exception {
        var e = createEventWithType(EventType.UPDATE_SCHOOL);
        delegator.handleEvent(e);
        verify(handler).handleSchoolEvent(e);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void moveSchool_giventMoveSchoolEventType_delegatesToHandleMoveSchoolEvent() throws Exception {
        var e = createEventWithType(EventType.MOVE_SCHOOL);
        delegator.handleEvent(e);
        verify(handler).handleMoveSchoolEvent(e);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void createGradSchool_givenCreateGradSchoolEventType_delegatesToHandleGradSchoolEvent() throws Exception {
        var e = createEventWithType(EventType.CREATE_GRAD_SCHOOL);
        delegator.handleEvent(e);
        verify(handler).handleGradSchoolEvent(e);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void updateGradSchool_giventUpdateGradSchoolEventType_delegatesToHandleGradSchoolEvent() throws Exception {
        var e = createEventWithType(EventType.UPDATE_GRAD_SCHOOL);
        delegator.handleEvent(e);
        verify(handler).handleGradSchoolEvent(e);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void defaultCase_givenEventNotListedInSwitch_silentlyIgnoresOtherEvents() {
        var e = createEventWithType(EventType.CREATE_AUTHORITY);
        delegator.handleEvent(e);
        verifyNoInteractions(handler);
    }

    @Test
    void exceptionFromDelegate_isCaughtAndNotRethrown() throws Exception {
        var e = createEventWithType(EventType.UPDATE_SCHOOL);
        doThrow(new RuntimeException("Huston, we have a problem.")).when(handler).handleSchoolEvent(e);
        assertDoesNotThrow(() -> delegator.handleEvent(e));
        verify(handler).handleSchoolEvent(e);
        verifyNoMoreInteractions(handler);
    }

}
