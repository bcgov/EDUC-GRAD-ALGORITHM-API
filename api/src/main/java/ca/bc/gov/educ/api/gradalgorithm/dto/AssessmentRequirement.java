package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class AssessmentRequirement {

	private UUID assessmentRequirementId;
	private String assessmentCode;   
	private AssessmentRequirementCode ruleCode;
}
