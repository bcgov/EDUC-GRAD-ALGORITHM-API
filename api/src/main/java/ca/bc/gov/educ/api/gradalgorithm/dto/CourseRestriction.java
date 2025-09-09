package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.UUID;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class CourseRestriction extends BaseRequest {

	private UUID courseRestrictionId;
	private String mainCourse; 
	private String mainCourseLevel;
	private String restrictedCourse; 
	private String restrictedCourseLevel;   
	private String restrictionStartDate; 
	private String restrictionEndDate;	
}
