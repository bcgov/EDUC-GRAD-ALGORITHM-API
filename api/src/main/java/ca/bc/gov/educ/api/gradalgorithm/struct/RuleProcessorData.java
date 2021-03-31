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
    private List<GradSpecialProgramRule> gradSpecialProgramRulesFrenchImmersion;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesAdvancedPlacement;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesInternationalBaccalaureateBD;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesInternationalBaccalaureateBC;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesCareerProgram;
    private List<StudentCourse> studentCourses;
    private List<StudentAssessment> studentAssessments;
    private List<CourseRequirement> courseRequirements;
    private List<GradLetterGrade> gradLetterGradeList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private boolean isGraduated;
    private boolean isSpecialProgramGraduated;
    private boolean hasSpecialProgramFrenchImmersion;
    private boolean hasSpecialProgramAdvancedPlacement;
    private boolean hasSpecialProgramInternationalBaccalaureate;
    private boolean hasSpecialProgramCareerProgram;
    private GradAlgorithmGraduationStatus gradStatus;
    private SpecialGradAlgorithmGraduationStatus gradSpecialProgramStatus;
    private School school;
    private boolean isProjected;

}
