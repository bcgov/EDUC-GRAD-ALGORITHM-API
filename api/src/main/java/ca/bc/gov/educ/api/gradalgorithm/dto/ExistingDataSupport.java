package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExistingDataSupport {
    private String existingProgramCompletionDate;
    private String existingGradMessage;
    private String gradProgram;

    // existing nonGrad reasons for optional programs
    private List<GradRequirement> optionalNonGradReasons;
}
