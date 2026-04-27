package ca.bc.gov.educ.api.gradalgorithm.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateUtils {

    private static final DateTimeFormatter PROGRAM_COMPLETION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS").withResolverStyle(ResolverStyle.STRICT);

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
            LocalDateTime parsedLocalDateTime = LocalDateTime.parse(programCompletionDate, PROGRAM_COMPLETION_DATE_FORMATTER);
            return parsedLocalDateTime.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
