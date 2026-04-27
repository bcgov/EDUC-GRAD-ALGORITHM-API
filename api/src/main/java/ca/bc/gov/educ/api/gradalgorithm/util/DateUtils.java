package ca.bc.gov.educ.api.gradalgorithm.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;

public class DateUtils {

    private static final DateTimeFormatter PROGRAM_COMPLETION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu/MM").withResolverStyle(ResolverStyle.STRICT);

    private DateUtils(){}

    public static LocalDate toLocalDate(Date date) {
        if(date == null) return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Date toDate(LocalDate localDate) {
        if(localDate == null) return null;
        return Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static LocalDate toProgramCompletionMonthEnd(String programCompletionDate) {
        if (programCompletionDate == null || programCompletionDate.isBlank()) {
            return null;
        }

        try {
            YearMonth parsedYearMonth = YearMonth.parse(programCompletionDate, PROGRAM_COMPLETION_DATE_FORMATTER);
            return parsedYearMonth.atEndOfMonth();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
