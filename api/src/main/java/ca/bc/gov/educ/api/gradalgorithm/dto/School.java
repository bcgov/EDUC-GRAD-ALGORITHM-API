package ca.bc.gov.educ.api.gradalgorithm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * This is a legacy object which does not conform to the newest
 * attribute set from the institute api. For a condensed version
 * use {@link ca.bc.gov.educ.api.gradalgorithm.dto.institute.School}
 * or for a more detailed object use {@link ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolDetail}
 * This should be deprecated.
 */
@Data
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class School {

	private String minCode;
	private String schoolId;
    private String schoolName;
	private String districtId;
    private String districtName;
    private String transcriptEligibility;    
    private String certificateEligibility;
    private String address1;    
    private String address2;    
    private String city;    
    private String provCode;
    private String countryCode;
    private String postal;
	private String openFlag;
	private String schoolCategoryCode;
	private String schoolCategoryLegacyCode;

	@Override
	public String toString() {
		return "School [minCode=" + minCode + ", schoolId=" + schoolId + ", schoolName=" + schoolName + ", schoolCategoryCode=" + schoolCategoryCode + ", schoolCategoryLegacyCode=" + schoolCategoryLegacyCode
				+ ", districtId=" + districtId + ", districtName=" + districtName + ", transcriptEligibility=" + transcriptEligibility + ", certificateEligibility=" + certificateEligibility
				+ ", address1=" + address1 + ", address2=" + address2 + ", city=" + city + ", provCode=" + provCode + ", countryCode=" + countryCode + ", postal=" + postal + ", openFlag=" + openFlag
				+ "]";
	}

}
