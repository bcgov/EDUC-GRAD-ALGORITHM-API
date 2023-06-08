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

    public boolean isPullGraduatedMessage() {
        if (StringUtils.isBlank(gradDate)) {
            return false;
        }
        String gradDateStr = gradDate.length() < 10? gradDate + "/01" : gradDate;
        log.debug("GradMessageRequest: Grad Date = {}", gradDateStr);
        SimpleDateFormat dateFormat = new SimpleDateFormat(gradDate.length() < 10? GradAlgorithmAPIConstants.SECONDARY_DATE_FORMAT : GradAlgorithmAPIConstants.DEFAULT_DATE_FORMAT);
        try {
            Date dt = dateFormat.parse(gradDateStr);
            Calendar calGradDate = Calendar.getInstance();
            calGradDate.setTime(dt);
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            return calGradDate.before(now);
        } catch (ParseException e) {
            log.error("Date Parse Exception: gradDate = {}. format = {}", gradDateStr, dateFormat.toPattern());
            return false;
        }
    }

}
