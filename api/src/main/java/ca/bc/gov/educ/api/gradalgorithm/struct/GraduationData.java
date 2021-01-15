package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@AllArgsConstructor
@Builder
public class GraduationData {
    private GradStudent gradStudent;
    private GradAlgorithmGraduationStatus gradStatus;
    private School school;
    private StudentCourses studentCourses;
    //Student Assessments
    //Student Exams
    List<String> nonGradReasons;
    List<String> requirementsMet;
    //Grad Message
    //Student Career Programs
    boolean isGraduated;
}
