package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dt = dateFormat.parse(gradDate);
            Calendar calGradDate = Calendar.getInstance();
            calGradDate.setTime(dt);
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            return calGradDate.before(now);
        } catch (ParseException e) {
            return false;
        }
    }

}
