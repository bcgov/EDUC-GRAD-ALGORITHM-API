package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradProgram {

    private String programCode;
    private String programName;
    private String programType;
    private String createdBy;
    private Date createdTimestamp;
    private String updatedBy;
    private Date updatedTimestamp;

}
