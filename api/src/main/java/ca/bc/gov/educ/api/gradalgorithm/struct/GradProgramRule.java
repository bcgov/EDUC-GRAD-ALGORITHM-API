package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.UUID;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradProgramRule {

	private UUID id;
    private String ruleCode; 
	private String requirementName;
	private String requirementType;
	private String requiredCredits;
	private String notMetDesc;
	private String requiredLevel;
	private String languageOfInstruction;
	private String requirementDesc;
	private String isActive;
	private UUID programSetID;
	private String createdBy;
	private Date createdTimestamp;
	private String updatedBy;
	private Date updatedTimestamp;
	
	@Override
	public String toString() {
		return "GradProgramRules [id=" + id + ", ruleCode=" + ruleCode + ", requirementName=" + requirementName
				+ ", requirementType=" + requirementType + ", requiredCredits=" + requiredCredits + ", notMetDesc="
				+ notMetDesc + ", requiredLevel=" + requiredLevel + ", languageOfInstruction=" + languageOfInstruction
				+ ", requirementDesc=" + requirementDesc + ", isActive=" + isActive + ", programSetID=" + programSetID
				+ ", createdBy=" + createdBy + ", createdTimestamp=" + createdTimestamp + ", updatedBy=" + updatedBy
				+ ", updatedTimestamp=" + updatedTimestamp + "]";
	}	
	
	
}
