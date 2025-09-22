package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("SchoolMove")
public class SchoolMove extends BaseRequest {

    private String schoolMoveId;
    private String toSchoolId;
    private String fromSchoolId;
    private String moveDate;

}