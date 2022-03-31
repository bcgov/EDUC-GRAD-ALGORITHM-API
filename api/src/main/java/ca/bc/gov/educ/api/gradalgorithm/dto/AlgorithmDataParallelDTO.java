package ca.bc.gov.educ.api.gradalgorithm.dto;

public class AlgorithmDataParallelDTO {
    private CourseAlgorithmData courseAlgorithmData;
    private AssessmentAlgorithmData assessmentAlgorithmData;
    private StudentGraduationAlgorithmData studentGraduationAlgorithmData;

    public AlgorithmDataParallelDTO(CourseAlgorithmData courseAlgorithmData, AssessmentAlgorithmData assessmentAlgorithmData, StudentGraduationAlgorithmData studentGraduationAlgorithmData) {
        this.courseAlgorithmData = courseAlgorithmData;
        this.assessmentAlgorithmData = assessmentAlgorithmData;
        this.studentGraduationAlgorithmData = studentGraduationAlgorithmData;
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

}
