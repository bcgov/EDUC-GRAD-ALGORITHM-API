package ca.bc.gov.educ.api.gradalgorithm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GradAlgorithmApiUtils {

    private static final Logger logger = LoggerFactory.getLogger(GradAlgorithmApiUtils.class);
    private static final String ERROR_MSG = "Error : {}";

    private GradAlgorithmApiUtils() {}

    public static Date parseDate(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            logger.error(ERROR_MSG,e.getMessage());
        }

        return date;
    }
}
