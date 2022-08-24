package ca.bc.gov.educ.api.gradalgorithm.dto;

public class AlgorithmDataParallelDTO {
    private CourseAlgorithmData courseAlgorithmData;
    private AssessmentAlgorithmData assessmentAlgorithmData;

    public AlgorithmDataParallelDTO(CourseAlgorithmData courseAlgorithmData, AssessmentAlgorithmData assessmentAlgorithmData) {
        this.courseAlgorithmData = courseAlgorithmData;
        this.assessmentAlgorithmData = assessmentAlgorithmData;
    }
    public AssessmentAlgorithmData assessmentAlgorithmData() {
        return  assessmentAlgorithmData;
    }

    public CourseAlgorithmData courseAlgorithmData() {
        return  courseAlgorithmData;
    }

}
