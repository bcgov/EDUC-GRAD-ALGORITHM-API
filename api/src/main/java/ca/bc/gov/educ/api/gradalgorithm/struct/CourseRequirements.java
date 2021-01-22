package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class CourseRequirements {
    private List<CourseRequirement> courseRequirementList;
}
