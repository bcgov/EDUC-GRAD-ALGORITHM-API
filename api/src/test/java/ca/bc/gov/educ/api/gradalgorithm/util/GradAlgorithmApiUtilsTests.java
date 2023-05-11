package ca.bc.gov.educ.api.gradalgorithm.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class GradAlgorithmApiUtilsTests {

    @Test
    public void testParseDateWithValidDate() throws ParseException {

        String testDate = "1995/01/01";
        String testDateFormat = "yyyy/MM/DD";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(testDateFormat);
        LocalDate actual = simpleDateFormat.parse(testDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate expected = GradAlgorithmApiUtils.parseDate("1995/01/01", testDateFormat);
        Assertions.assertEquals(expected, actual);
    }
}
