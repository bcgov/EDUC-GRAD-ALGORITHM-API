package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.List;

import lombok.Data;

@Data
public class StudentGraduationAlgorithmData {

	private List<LetterGrade> letterGrade;
	private List<SpecialCase> specialCase;
	private List<ProgramAlgorithmRule> programAlgorithmRules;
}
