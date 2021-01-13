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
    private ProgramRules programRules;
    private StudentCourses studentCourses;
    private CourseRequirements courseRequirements;
    private boolean passed = false;
    private List<String> passMessages;
    private List<String> failedMessages;

    public MatchRuleData (ProgramRules programRules, StudentCourses studentCourses, CourseRequirements courseRequirements) {
        this.programRules = programRules;
        this.studentCourses = studentCourses;
        this.courseRequirements = courseRequirements;
    }
}
