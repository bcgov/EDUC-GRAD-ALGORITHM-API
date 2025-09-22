package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import ca.bc.gov.educ.api.gradalgorithm.dto.v2.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("neighborhoodLearning")
public class NeighborhoodLearning extends BaseRequest {

    private String neighborhoodLearningId;
    private String schoolId;
    private String neighborhoodLearningTypeCode;

}
