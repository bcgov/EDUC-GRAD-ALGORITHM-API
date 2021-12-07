package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradMessageRequest {
    String gradProgram;
    String msgType;
    String gradDate;
    String honours;
    String programName;
    boolean projected;
}
