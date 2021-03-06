package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class SpecialGradAlgorithmGraduationStatus {

	private String pen;
    private UUID specialProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
    private StudentCourses specialStudentCourses;
    private StudentAssessments specialStudentAssessments;
    private boolean isSpecialGraduated;
    private List<GradRequirement> specialNonGradReasons;
    private List<GradRequirement> specialRequirementsMet;
    private UUID studentID;
}
