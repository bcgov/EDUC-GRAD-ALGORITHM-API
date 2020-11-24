package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class ProgramRules {
    List<ProgramRule> programRuleList;
}
