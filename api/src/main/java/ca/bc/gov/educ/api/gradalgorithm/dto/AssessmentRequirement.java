package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class AssessmentRequirement {

	private UUID assessmentRequirementId;
	private String assessmentCode;   
	private AssessmentRequirementCode ruleCode;
}
