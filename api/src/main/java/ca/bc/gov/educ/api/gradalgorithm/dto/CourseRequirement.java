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
public class CourseRequirement {

	private UUID courseRequirementId;
	private String courseCode;
    private String courseLevel;
    private CourseRequirementCodeDTO ruleCode;
    private String courseName;

    public String getCourseCode() {
        if (courseCode != null)
            courseCode = courseCode.trim();
        return courseCode;
    }

    public String getCourseLevel() {
        if (courseLevel != null)
            courseLevel = courseLevel.trim();
        return courseLevel;
    }
    
    public String getCourseName() {
    	return courseName != null ? courseName.trim():null;
    }
}
