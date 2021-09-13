package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class AssessmentRequirementCode {

	private String assmtRequirementCode;
	private String label;
	private String description;
	private Date effectiveDate;
	private Date expiryDate;
}
