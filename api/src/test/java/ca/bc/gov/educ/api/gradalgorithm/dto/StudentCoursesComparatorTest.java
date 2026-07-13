package ca.bc.gov.educ.api.gradalgorithm.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StudentCoursesComparatorTest {

    @Test
    public void compare_shouldSort2023EnglishCoursesLike2018Program() {
        List<StudentCourse> studentCourses = new ArrayList<>();
        studentCourses.add(createCourse("YPHR", "10C", 77.0, 4, "2024/03"));
        studentCourses.add(createCourse("WWK", "10", 91.0, 4, "2024/03"));
        studentCourses.add(createCourse("PHED", "10", 94.0, 4, "2024/05"));

        studentCourses.sort(new StudentCoursesComparator("2023-EN"));

        assertEquals("PHED", studentCourses.get(0).getCourseCode());
        assertEquals("WWK", studentCourses.get(1).getCourseCode());
        assertEquals("YPHR", studentCourses.get(2).getCourseCode());
    }

    @Test
    public void compare_shouldUseEarlierSessionDateAsTieBreakerFor2023Programs() {
        List<StudentCourse> studentCourses = new ArrayList<>();
        studentCourses.add(createCourse("CLC", "B", 91.0, 4, "2026/03"));
        studentCourses.add(createCourse("CLC", "A", 91.0, 4, "2024/11"));

        studentCourses.sort(new StudentCoursesComparator("2023-EN"));

        assertEquals("A", studentCourses.get(0).getCourseLevel());
        assertEquals("2024/11", studentCourses.get(0).getSessionDate());
        assertEquals("B", studentCourses.get(1).getCourseLevel());
    }

    private StudentCourse createCourse(String code, String level, Double percent, Integer credits, String sessionDate) {
        StudentCourse course = new StudentCourse();
        course.setCourseCode(code);
        course.setCourseLevel(level);
        course.setCompletedCoursePercentage(percent);
        course.setCredits(credits);
        course.setSessionDate(sessionDate);
        return course;
    }
}
