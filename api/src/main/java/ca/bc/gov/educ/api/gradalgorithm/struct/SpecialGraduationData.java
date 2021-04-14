package ca.bc.gov.educ.api.gradalgorithm.struct;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialGraduationData {
    private GradStudent gradStudent;
    private SpecialGradAlgorithmGraduationStatus gradStatus;
    private StudentCourses studentCourses;
    private StudentAssessments studentAssessments;
    private StudentExams studentExams;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private boolean isGraduated;
}
