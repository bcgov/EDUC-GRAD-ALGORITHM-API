package ca.bc.gov.educ.api.gradalgorithm.util;

import lombok.extern.slf4j.Slf4j;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class GradAlgorithmApiUtils {

    private static final String ERROR_MSG = "Error : {}";

    private GradAlgorithmApiUtils() {}

    public static Date parseDate(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            log.error(ERROR_MSG,e.getMessage());
        }

        return date;
    }
}
