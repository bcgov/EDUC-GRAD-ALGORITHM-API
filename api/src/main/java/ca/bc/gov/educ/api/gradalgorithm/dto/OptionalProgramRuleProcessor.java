package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class OptionalProgramRuleProcessor {

	private List<OptionalProgramRequirement> optionalProgramRules;
	private List<StudentCourse> studentCoursesOptionalProgram;
	private List<StudentAssessment> studentAssessmentsOptionalProgram;
	private List<GradRequirement> nonGradReasonsOptionalProgram;
	private List<GradRequirement> requirementsMetOptionalProgram;
	private String studentOptionalProgramData;
	private String optionalProgramName;
	private boolean isOptionalProgramGraduated;
	private boolean hasOptionalProgram;
}
