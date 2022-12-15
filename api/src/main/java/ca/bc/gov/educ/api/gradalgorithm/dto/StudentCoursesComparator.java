package ca.bc.gov.educ.api.gradalgorithm.dto;
import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class StudentCoursesComparator implements Comparator<StudentCourse> {

    private String program;
    public StudentCoursesComparator(String program) {
        this.program = program;
    }

    @Override
    public int compare(StudentCourse o1, StudentCourse o2) {
        int res = 0;
        switch (program) {
            case "2018-EN":
            case "2018-PF":
            case "2004-EN":
            case "2004-PF":
                res= new CompareToBuilder()
                        .append(o2.getCompletedCoursePercentage(), o1.getCompletedCoursePercentage())
                        .append(o2.getCredits(), o1.getCredits())
                        .append(o1.getSessionDate(), o2.getSessionDate())
                        .append(o2.getCourseLevel(), o1.getCourseLevel()).toComparison();
                break;
            default:

        }
        return res;

    }
}