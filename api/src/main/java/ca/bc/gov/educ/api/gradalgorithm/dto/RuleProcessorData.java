package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleProcessorData {

    private GradSearchStudent gradStudent;
    private List<ProgramRequirement> gradProgramRules;
    private Map<String,OptionalProgramRuleProcessor> mapOptional;
    private List<StudentCourse> studentCourses;
    private List<StudentAssessment> studentAssessments;
    private List<CourseRequirement> courseRequirements;
    private List<CourseRestriction> courseRestrictions;
    private List<AssessmentRequirement> assessmentRequirements;
    private List<Assessment> assessmentList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private boolean isGraduated;
    private GradAlgorithmGraduationStudentRecord gradStatus;
    private GradAlgorithmOptionalStudentProgram gradOptionalProgramStatus;
    private GraduationProgramCode gradProgram;
    private School school;
    private boolean isProjected;    
    private List<LetterGrade> letterGradeList;
    private List<SpecialCase> specialCaseList;
    private List<ProgramAlgorithmRule> algorithmRules;
}
