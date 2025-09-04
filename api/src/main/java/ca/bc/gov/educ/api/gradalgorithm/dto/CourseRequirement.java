package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.sql.Date;
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
public class CourseRequirement extends BaseRequest {

	private UUID courseRequirementId;
	private String courseCode;
    private String courseLevel;
    private CourseRequirementCodeDTO ruleCode;
    private String courseName;
    private Date startDate;
    private Date endDate;
    private String completionEndDate;

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
