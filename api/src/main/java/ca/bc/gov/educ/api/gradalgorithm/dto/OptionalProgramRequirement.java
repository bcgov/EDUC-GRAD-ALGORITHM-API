package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class OptionalProgramRequirement {

	private UUID optionalProgramRequirementID; 
	private UUID optionalProgramID; 
	private OptionalProgramRequirementCode optionalProgramRequirementCode;
}
