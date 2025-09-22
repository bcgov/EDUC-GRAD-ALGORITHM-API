package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("instituteDistrict")
@NoArgsConstructor
@AllArgsConstructor
public class District extends BaseRequest {

    private String districtId;
    private String districtNumber;
    private String faxNumber;
    private String phoneNumber;
    private String email;
    private String website;
    private String displayName;
    private String districtRegionCode;
    private String districtStatusCode;
    private List<DistrictContact> contacts;
    private List<DistrictAddress> addresses;
    private List<Note> notes;

}
