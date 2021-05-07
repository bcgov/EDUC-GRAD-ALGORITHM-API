package ca.bc.gov.educ.api.gradalgorithm.struct;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentList {

	List<String> assessmentCodes;		
}
