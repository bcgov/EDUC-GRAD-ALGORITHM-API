package ca.bc.gov.educ.api.gradalgorithm.dto;

public class AlgorithmDataParallelDTO {
    private CourseAlgorithmData courseAlgorithmData;
    private AssessmentAlgorithmData assessmentAlgorithmData;
    private StudentGraduationAlgorithmData studentGraduationAlgorithmData;
    private School schoolData;

    public AlgorithmDataParallelDTO(CourseAlgorithmData courseAlgorithmData, AssessmentAlgorithmData assessmentAlgorithmData, StudentGraduationAlgorithmData studentGraduationAlgorithmData,School schoolData) {
        this.courseAlgorithmData = courseAlgorithmData;
        this.assessmentAlgorithmData = assessmentAlgorithmData;
        this.studentGraduationAlgorithmData = studentGraduationAlgorithmData;
        this.schoolData = schoolData;
    }

    public StudentGraduationAlgorithmData studentGraduationAlgorithmData() {
        return  studentGraduationAlgorithmData;
    }

    public AssessmentAlgorithmData assessmentAlgorithmData() {
        return  assessmentAlgorithmData;
    }

    public CourseAlgorithmData courseAlgorithmData() {
        return  courseAlgorithmData;
    }

    public School schoolData() {
        return  schoolData;
    }

}
