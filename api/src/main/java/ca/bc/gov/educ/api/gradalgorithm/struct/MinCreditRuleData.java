package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class MinCreditRuleData {
    private ProgramRule programRule;
    private StudentCourses studentCourses;
    private int acquiredCredits;
    private int requiredCredits;
    private boolean passed;
}
