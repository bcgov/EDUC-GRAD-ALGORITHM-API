package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("note")
public class Note extends BaseRequest {

    private String noteId;
    private String schoolId;
    private String districtId;
    private String independentAuthorityId;
    private String content;

}
