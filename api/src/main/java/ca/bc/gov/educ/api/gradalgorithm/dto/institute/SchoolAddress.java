package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("schoolAddress")
public class SchoolAddress extends BaseRequest {

    private String SchoolAddressId;
    private String SchoolId;
    private String addressTypeCode;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postal;
    private String provinceCode;
    private String countryCode;

}
