package ca.bc.gov.educ.api.gradalgorithm.dto;

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
public class GradProgramSet {

	private UUID id;	
	private String programSet;
	private String programSetName;	
	private String gradProgramCode;	
	private String programSetStartDate;	
	private String programSetEndDate;
	private String createdBy;	
	private Date createdTimestamp;	
	private String updatedBy;	
	private Date updatedTimestamp;
	
	@Override
	public String toString() {
		return "GradProgramSet [id=" + id + ", programSet=" + programSet + ", programSetName=" + programSetName
				+ ", gradProgramCode=" + gradProgramCode + ", createdBy=" + createdBy + ", createdTimestamp="
				+ createdTimestamp + ", updatedBy=" + updatedBy + ", updatedTimestamp=" + updatedTimestamp + "]";
	}		
}
