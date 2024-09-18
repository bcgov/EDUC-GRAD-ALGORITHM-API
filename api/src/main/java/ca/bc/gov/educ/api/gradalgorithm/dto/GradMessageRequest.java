package ca.bc.gov.educ.api.gradalgorithm.dto;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        if (StringUtils.isBlank(gradDate)) {
            return false;
        }
        String gradDateStr = gradDate.length() < 10 ? gradDate + "/01" : gradDate;
        log.debug("GradMessageRequest: Grad Date = {}", gradDateStr);
        SimpleDateFormat dateFormat = new SimpleDateFormat(gradDate.length() < 10? GradAlgorithmAPIConstants.SECONDARY_DATE_FORMAT : GradAlgorithmAPIConstants.DEFAULT_DATE_FORMAT);
        try {
            Date dt = dateFormat.parse(gradDateStr);
            Calendar calGradDate = Calendar.getInstance();
            calGradDate.setTime(dt);
            calGradDate.set(Calendar.DAY_OF_MONTH, calGradDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            return calGradDate.before(now);
        } catch (ParseException e) {
            log.error("Date Parse Exception: gradDate = {}. format = {}", gradDateStr, dateFormat.toPattern());
            return false;
        }
    }

}
