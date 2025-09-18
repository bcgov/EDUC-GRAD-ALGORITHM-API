package ca.bc.gov.educ.api.gradalgorithm.mapper.v1;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class MapperHelpers {

    @Named("boolToYN")
    public String boolToYN(Boolean b) {
        if (b == null) return null; // or "N" if you prefer a default
        return b ? "Y" : "N";
    }

}
