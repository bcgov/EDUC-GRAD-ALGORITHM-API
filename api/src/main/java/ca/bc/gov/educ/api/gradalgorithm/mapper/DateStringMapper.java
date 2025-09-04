package ca.bc.gov.educ.api.gradalgorithm.mapper;

import lombok.extern.slf4j.Slf4j;
import java.sql.Date;
import java.time.LocalDateTime;

@Slf4j
public class DateStringMapper {
    
    public static Date map(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            // Parse ISO datetime format
            LocalDateTime localDateTime = LocalDateTime.parse(dateString);
            return Date.valueOf(localDateTime.toLocalDate());
        } catch (Exception e) {
            log.warn("Invalid date format: {}", dateString);
            throw e;
        }
    }
}
