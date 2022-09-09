package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.List;

import lombok.Data;

@Data
public class StudentGraduationAlgorithmData {

	private String gradProgram;
	private List<LetterGrade> letterGrade;
	private List<SpecialCase> specialCase;
	private List<ProgramAlgorithmRule> programAlgorithmRules;
	private TranscriptMessage graduatedMessage;
	private TranscriptMessage nonGraduateMessage;
}
