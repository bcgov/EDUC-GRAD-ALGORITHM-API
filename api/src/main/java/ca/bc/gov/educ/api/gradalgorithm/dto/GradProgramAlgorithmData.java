package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class GradProgramAlgorithmData {

	private String programKey;
	private UUID optionalProgramID;
	private List<ProgramRequirement> programRules;
	private List<OptionalProgramRequirement> optionalProgramRules;
	private GraduationProgramCode gradProgram;
}
