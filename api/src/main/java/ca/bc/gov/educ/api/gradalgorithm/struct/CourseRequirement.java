package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Data
@Component
public class CourseRequirement {

    private UUID courseRequirementId;
    private String courseCode;
    private String courseLevel;
    private String ruleCode;
    private String createdBy;
    private Date createdTimestamp;
    private String updatedBy;
    private Date updatedTimestamp;

    @Override
    public String toString() {
        return "\nCourseRequirement {" +
                "courseRequirementId='" + courseRequirementId + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", courseLevel='" + courseLevel + '\'' +
                ", ruleCode=" + ruleCode +
                "}";
    }

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
}
