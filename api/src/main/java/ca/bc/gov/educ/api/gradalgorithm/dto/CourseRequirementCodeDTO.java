package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.sql.Date;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class CourseRequirementCodeDTO extends BaseRequest {
    private String courseRequirementCode;
    private String label;
    private String description;
    private Date effectiveDate;
    private Date expiryDate;
}
