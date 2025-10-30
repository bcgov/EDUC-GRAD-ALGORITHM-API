package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
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
    void handleSchoolEvent_updatesCacheWithSchoolId() throws Exception {
        String schoolId = UUID.randomUUID().toString();
        var school = new School();
        school.setSchoolId(schoolId);
        var event = Event.builder().eventType(EventType.UPDATE_SCHOOL).eventPayload(JsonTransformer.getJsonStringFromObject(school)).build();
        service.handleSchoolEvent(event);
        verify(gradSchoolService).updateSchoolInCache(schoolId);
        verifyNoMoreInteractions(gradSchoolService);
    }

}
