package ca.bc.gov.educ.api.gradalgorithm.mapper.v1;

import ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolAddress;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchoolMapperHelpers {

    @Named("boolToYN")
    public String boolToYN(Boolean b) {
        if (b == null) return null; // or "N" if you prefer a default
        return b ? "Y" : "N";
    }

    @Named("firstAddressLine1")
    public String firstAddressLine1(List<SchoolAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        return addresses.get(0).getAddressLine1();
    }

    @Named("firstAddressLine2")
    public String firstAddressLine2(List<SchoolAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        return addresses.get(0).getAddressLine2();
    }

    @Named("firstCity")
    public String fistCity(List<SchoolAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        return addresses.get(0).getCity();
    }

    @Named("firstProvCode")
    public String fistProvCode(List<SchoolAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        return addresses.get(0).getProvinceCode();
    }

    @Named("firstCountryCode")
    public String fistCountryCode(List<SchoolAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        return addresses.get(0).getCountryCode();
    }

    @Named("firstPostalCode")
    public String fistPostalCode(List<SchoolAddress> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        return addresses.get(0).getPostal();
    }

    @Named("openFlag")
    public String getOpenFlag(String closedDate){
        return boolToYN(closedDate == null);
    }

}
