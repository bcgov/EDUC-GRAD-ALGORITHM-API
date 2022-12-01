package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class GradMessageRequest {
    String gradProgram;
    String msgType;
    String gradDate;
    String honours;
    String programName;
    String schoolAtGradName;
    boolean projected;

    public boolean isPullGraduatedMessage() {
        return StringUtils.isNotBlank(gradDate);
    }
}
