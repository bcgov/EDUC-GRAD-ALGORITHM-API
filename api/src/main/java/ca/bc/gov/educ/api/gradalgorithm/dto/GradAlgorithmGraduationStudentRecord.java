package ca.bc.gov.educ.api.gradalgorithm.dto;

import ca.bc.gov.educ.api.gradalgorithm.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.time.LocalDate;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.DEFAULT_DATE_FORMAT;

@Data
@Component
public class GradAlgorithmGraduationStudentRecord {

	private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private String studentGrade;	
    private String studentStatus;
    private UUID studentID;
    private String schoolAtGrad;
    private String studentCitizenship;
    private String consumerEducationRequirementMet;
    private String schoolAtGradName;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private LocalDate adultStartDate;

    public Date getAdultStartDate() {
        return DateUtils.toDate(adultStartDate);
    }
}
