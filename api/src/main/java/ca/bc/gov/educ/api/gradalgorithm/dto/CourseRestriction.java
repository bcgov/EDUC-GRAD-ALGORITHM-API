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
public class CourseRestriction {

	private UUID courseRestrictionId;
	private String mainCourse; 
	private String mainCourseLevel;
	private String restrictedCourse; 
	private String restrictedCourseLevel;   
	private String restrictionStartDate; 
	private String restrictionEndDate;	
}
