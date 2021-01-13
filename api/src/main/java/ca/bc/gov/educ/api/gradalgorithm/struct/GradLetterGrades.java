package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class GradLetterGrades {
    private List<GradLetterGrade> gradLetterGradeList;
}
