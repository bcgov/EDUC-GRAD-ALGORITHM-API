package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleProcessorData {

    private GradStudent gradStudent;
    private List<GradAlgorithmRules> gradAlgorithmRules;
    private List<GradProgramRule> gradProgramRules;
    private List<StudentCourse> studentCourses;
    private List<StudentAssessment> studentAssessments;
    private List<CourseRequirement> courseRequirements;
    private List<GradLetterGrade> gradLetterGradeList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private boolean isGraduated;
    private GradAlgorithmGraduationStatus gradStatus;
    private School school;
    private boolean isProjected;

}
