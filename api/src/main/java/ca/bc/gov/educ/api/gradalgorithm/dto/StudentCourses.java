package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourses {
    private List<StudentCourse> studentCourseList;

    @Override
    public String toString() {
        StringBuffer output = new StringBuffer("");

        for (StudentCourse sc : studentCourseList) {
            output.append(sc.toString())
            .append("\n");
        }
        return output.toString();
    }
}
