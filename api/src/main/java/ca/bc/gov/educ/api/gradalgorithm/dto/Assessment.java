package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Assessment {

	private String assessmentCode;
    private String assessmentName;
    private String language;    
    private Date startDate;
    private Date endDate;
    
	@Override
	public String toString() {
		return "Assessment [assessmentCode=" + assessmentCode + ", assessmentName=" + assessmentName + ", language="
				+ language + ", startDate=" + startDate + ", endDate=" + endDate + "]";
	}
    
			
}
