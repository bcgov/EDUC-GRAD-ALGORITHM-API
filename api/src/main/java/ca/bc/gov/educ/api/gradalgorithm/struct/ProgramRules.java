package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramRules {
    private List<ProgramRule> programRuleList;
}
