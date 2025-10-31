package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSchool;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EventHandlerServiceTest {

    @Mock
    private GradSchoolService gradSchoolService;
    @InjectMocks
    private EventHandlerService service;

    @Test
    void handleSchoolEvent_givenUpdatedSchoolPayload_updatesCacheWithSchoolId() throws Exception {
        var school = new School();
        school.setSchoolId(UUID.randomUUID().toString());
        var event = Event.builder().eventType(EventType.UPDATE_SCHOOL).eventPayload(JsonTransformer.getJsonStringFromObject(school)).build();
        service.handleSchoolEvent(event);
        verify(gradSchoolService).updateSchoolInCache(school.getSchoolId());
        verifyNoMoreInteractions(gradSchoolService);
    }

    @Test
    void handleGradSchoolEvent_givenUpdatedGradSchoolPayload_updatesCacheWithSchoolId() throws Exception {
        var gradSchool = new GradSchool();
        gradSchool.setSchoolID(UUID.randomUUID().toString());
        var event = Event.builder().eventType(EventType.UPDATE_GRAD_SCHOOL).eventPayload(JsonTransformer.getJsonStringFromObject(gradSchool)).build();
        service.handleGradSchoolEvent(event);
        verify(gradSchoolService).updateSchoolInCache(gradSchool.getSchoolID());
        verifyNoMoreInteractions(gradSchoolService);
    }

    @Test
    void handleMoveSchoolEvent_givenMovedSchoolPayload_updatesCacheWithSchoolId() throws Exception {
        var movedSchool = new MoveSchoolData();
        movedSchool.setFromSchoolId(UUID.randomUUID().toString());
        var school = new School();
        school.setSchoolId(UUID.randomUUID().toString());
        movedSchool.setToSchool(school);
        var event = Event.builder().eventType(EventType.MOVE_SCHOOL).eventPayload(JsonTransformer.getJsonStringFromObject(movedSchool)).build();
        service.handleMoveSchoolEvent(event);
        verify(gradSchoolService).updateSchoolInCache(movedSchool.getFromSchoolId());
        verify(gradSchoolService).updateSchoolInCache(school.getSchoolId());
        verifyNoMoreInteractions(gradSchoolService);
    }

}
