package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MatchRuleData {
    private GradProgramRules gradProgramRules;
    private StudentCourses studentCourses;
    private CourseRequirements courseRequirements;
    private boolean passed = false;
    private List<GradRequirement> passMessages;
    private List<GradRequirement> failMessages;

    public MatchRuleData (GradProgramRules programRules, StudentCourses studentCourses, CourseRequirements courseRequirements) {
        this.gradProgramRules = programRules;
        this.studentCourses = studentCourses;
        this.courseRequirements = courseRequirements;
    }
}
