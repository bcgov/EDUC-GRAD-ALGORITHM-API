package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component("SchoolCategoryCode")
public class SchoolCategoryCode {

    private String schoolCategoryCode;
    private String label;
    private String description;
    private String legacyCode;
    private String displayOrder;
    private String effectiveDate;
    private String expiryDate;

}
