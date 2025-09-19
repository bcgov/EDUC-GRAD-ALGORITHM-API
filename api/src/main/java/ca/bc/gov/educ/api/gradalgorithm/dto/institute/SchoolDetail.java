package ca.bc.gov.educ.api.gradalgorithm.dto.institute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("SchoolDetail")
public class SchoolDetail extends School {

    List<SchoolContact> contacts;
    List<SchoolAddress> addresses;
    List<Note> notes;
    List<Grade> grades;
    List<SchoolFundingGroup> schoolFundingGroups;
    List<NeighborhoodLearning> neighborhoodLearnings;
    List<SchoolMove> schoolMoves;

}
