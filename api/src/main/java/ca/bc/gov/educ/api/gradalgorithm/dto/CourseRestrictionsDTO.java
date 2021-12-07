package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class CourseRestrictionsDTO {
    List<CourseRestriction> courseRestrictions;
}
