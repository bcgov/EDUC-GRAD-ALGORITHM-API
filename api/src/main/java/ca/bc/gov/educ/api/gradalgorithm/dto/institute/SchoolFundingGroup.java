package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("SchoolFundingGroup")
public class SchoolFundingGroup extends BaseRequest {

    private String schoolFundingGroupID;
    private String schoolId;
    private String schoolGradeCode;
    private String schoolFundingGroupCode;

}
