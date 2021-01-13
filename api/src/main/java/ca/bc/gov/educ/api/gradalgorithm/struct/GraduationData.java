package ca.bc.gov.educ.api.gradalgorithm.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@AllArgsConstructor
@Builder
public class GraduationData {
    private GradStudent gradStudent;
    private StudentGradStatus gradStatus;
    private School school;
    private StudentCourses studentCourses;
    //Student Assessments
    //Student Exams
    //Non-Grad Reasons
    //Requirements Met
    //Grad Message
    //Student Career Programs
}
