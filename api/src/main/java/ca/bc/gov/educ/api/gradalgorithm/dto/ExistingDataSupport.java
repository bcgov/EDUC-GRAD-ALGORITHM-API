package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExistingDataSupport {
    private String existingProgramCompletionDate;
    private String existingGradMessage;
    private String gradProgram;

}
