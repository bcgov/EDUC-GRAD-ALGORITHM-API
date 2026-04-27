package ca.bc.gov.educ.api.gradalgorithm.dto;

import ca.bc.gov.educ.api.gradalgorithm.util.DateUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

@Slf4j
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
    boolean existingGraduated;
    boolean graduated;
    boolean existingOptionalProgramGraduated;

    public static final String SCCP_GRAD_PROGRAM = "SCCP";
    /**
     *
      * @return   true   "has graduated"
     *            false  "should be able to graduate" (projected to graduate)
     */
    public boolean isPullGraduatedMessage() {
        if (isPreviouslyGraduated()) { // "has graduated"
            return true;
        }
        if (projected && graduated) { // "should be able to graduate" (projected to graduated)
            return false;
        }
        return graduated;
    }

    public boolean isPreviouslyGraduated() {
        return existingGraduated && isGradDatePast();
    }

    private boolean isGradDatePast() {
        if (!StringUtils.equalsIgnoreCase(gradProgram, SCCP_GRAD_PROGRAM)) {
            return true;
        }
        // Only for SCCP to check the grad_date is future dated or not to determine graduation
        LocalDate gradCompletionDate = DateUtils.toProgramCompletionMonthEnd(gradDate);
        if (gradCompletionDate == null) {
            log.error("Date Parse Exception: gradDate = {}. Supported format: yyyy-MM-dd HH:mm:ss.SSS", gradDate);
            return false;
        }
        return !gradCompletionDate.isAfter(LocalDate.now());
    }

}
