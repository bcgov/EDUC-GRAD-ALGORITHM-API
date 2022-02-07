package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradStudentAlgorithmData {

	private GradSearchStudent gradStudent;
	private GradAlgorithmGraduationStudentRecord graduationStudentRecord;
	private List<StudentCareerProgram> studentCareerProgramList;
}
