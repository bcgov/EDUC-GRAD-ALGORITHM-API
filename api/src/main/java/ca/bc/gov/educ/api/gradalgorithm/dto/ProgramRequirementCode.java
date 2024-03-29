package ca.bc.gov.educ.api.gradalgorithm.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ProgramRequirementCode {

	private String proReqCode; 
	private String label; 
	private String description;
	private RequirementTypeCode requirementTypeCode;
	private String requiredCredits;
	private String notMetDesc;
	private String requiredLevel;
	private String languageOfInstruction;
	private String activeRequirement;
	private String requirementCategory;
	private String traxReqNumber;
}
