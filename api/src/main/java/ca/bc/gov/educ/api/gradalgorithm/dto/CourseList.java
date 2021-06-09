package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class CourseList {
    List<String> courseCodes;
}