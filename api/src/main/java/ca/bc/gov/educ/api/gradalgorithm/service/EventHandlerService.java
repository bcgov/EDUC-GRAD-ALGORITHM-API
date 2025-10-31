package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSchool;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventHandlerService {

    private final GradSchoolService gradSchoolService;

    @Autowired
    public EventHandlerService(GradSchoolService gradSchoolService) {
        this.gradSchoolService = gradSchoolService;
    }

    public void handleSchoolEvent(Event event) throws JsonProcessingException {
        var school = JsonTransformer.getJsonObjectFromString(School.class, event.getEventPayload());
        gradSchoolService.updateSchoolInCache(school.getSchoolId());
    }

    public void handleGradSchoolEvent(Event event) throws JsonProcessingException {
        var gradSchool = JsonTransformer.getJsonObjectFromString(GradSchool.class, event.getEventPayload());
        gradSchoolService.updateSchoolInCache(gradSchool.getSchoolID());
    }


    public void handleMoveSchoolEvent(Event event) throws JsonProcessingException {
        var movedSchool = JsonTransformer.getJsonObjectFromString(MoveSchoolData.class, event.getEventPayload());
        gradSchoolService.updateSchoolInCache(movedSchool.getToSchool().getSchoolId());
        gradSchoolService.updateSchoolInCache(movedSchool.getFromSchoolId());
    }
}
