package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class AssessmentAlgorithmData {
    List<StudentAssessment> studentAssessments;
    List<AssessmentRequirement> assessmentRequirements;
    List<Assessment> assessments;
}
