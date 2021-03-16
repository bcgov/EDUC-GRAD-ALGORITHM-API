package ca.bc.gov.educ.api.gradalgorithm.struct;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class SpecialMinElectiveCreditRuleData {
    private GradSpecialProgramRules gradSpecialProgramRules;
    private StudentCourses studentCourses;
    private boolean passed;
    private List<GradRequirement> passMessages;
    private List<GradRequirement> failMessages;
}
