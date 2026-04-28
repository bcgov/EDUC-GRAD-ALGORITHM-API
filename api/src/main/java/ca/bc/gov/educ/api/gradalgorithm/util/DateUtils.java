package ca.bc.gov.educ.api.gradalgorithm.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Date;

public class DateUtils {

    private static final DateTimeFormatter PROGRAM_COMPLETION_YEAR_MONTH_SLASH_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu/MM").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter PROGRAM_COMPLETION_YEAR_MONTH_DASH_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter PROGRAM_COMPLETION_LOCAL_DATE_SLASH_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu/MM/dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter PROGRAM_COMPLETION_LOCAL_DATE_DASH_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter PROGRAM_COMPLETION_TIMESTAMP_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("uuuu-MM-dd HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                    .optionalEnd()
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);

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

        LocalDateTime parsedLocalDateTime = parseLocalDateTime(programCompletionDate);
        if (parsedLocalDateTime != null) {
            LocalDate parsedDate = parsedLocalDateTime.toLocalDate();
            return parsedDate.withDayOfMonth(parsedDate.lengthOfMonth());
        }

        LocalDate parsedLocalDate = parseLocalDate(programCompletionDate);
        if (parsedLocalDate != null) {
            return parsedLocalDate.withDayOfMonth(parsedLocalDate.lengthOfMonth());
        }

        YearMonth parsedYearMonth = parseYearMonth(programCompletionDate);
        if (parsedYearMonth != null) {
            return parsedYearMonth.atEndOfMonth();
        }

        return null;
    }

    private static LocalDateTime parseLocalDateTime(String programCompletionDate) {
        try {
            return LocalDateTime.parse(programCompletionDate, PROGRAM_COMPLETION_TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalDate parseLocalDate(String programCompletionDate) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{PROGRAM_COMPLETION_LOCAL_DATE_DASH_FORMATTER, PROGRAM_COMPLETION_LOCAL_DATE_SLASH_FORMATTER}) {
            try {
                return LocalDate.parse(programCompletionDate, formatter);
            } catch (DateTimeParseException e) {
                // Try the next supported pattern.
            }
        }
        return null;
    }

    private static YearMonth parseYearMonth(String programCompletionDate) {
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{PROGRAM_COMPLETION_YEAR_MONTH_SLASH_FORMATTER, PROGRAM_COMPLETION_YEAR_MONTH_DASH_FORMATTER}) {
            try {
                return YearMonth.parse(programCompletionDate, formatter);
            } catch (DateTimeParseException e) {
                // Try the next supported pattern.
            }
        }
        return null;
    }

}
