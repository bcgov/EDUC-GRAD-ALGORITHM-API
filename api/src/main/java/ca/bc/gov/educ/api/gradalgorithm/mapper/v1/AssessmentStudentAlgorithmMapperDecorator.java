package ca.bc.gov.educ.api.gradalgorithm.mapper.v1;

import ca.bc.gov.educ.api.gradalgorithm.constants.ProvincialSpecialCaseCodes;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentStudentListItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
public abstract class AssessmentStudentAlgorithmMapperDecorator implements AssessmentStudentAlgorithmMapper {
    private final AssessmentStudentAlgorithmMapper delegate;

    protected AssessmentStudentAlgorithmMapperDecorator(AssessmentStudentAlgorithmMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public StudentAssessment toAlgorithmData(AssessmentStudentListItem easStudent) {
        final var assessmentStudentAlgorithm = this.delegate.toAlgorithmData(easStudent);
        setWroteFlag(easStudent, assessmentStudentAlgorithm);
        if(easStudent.getNumberOfAttempts() != null) {
            assessmentStudentAlgorithm.setExceededWriteFlag(
                Integer.parseInt(Optional.of(easStudent.getNumberOfAttempts()).orElse("0")) >= 3 ? "Y" : "N"
            );
        } else {
            assessmentStudentAlgorithm.setExceededWriteFlag("N");
        }
        assessmentStudentAlgorithm.setSessionDate(easStudent.getCourseYear() + "/"
            + easStudent.getCourseMonth());
        return assessmentStudentAlgorithm;
    }

    private void setWroteFlag(AssessmentStudent entity, StudentAssessment assessmentStudent) {
        boolean hasProficiencyScore = entity.getProficiencyScore() != null;
        boolean hasSpecialCaseCode = StringUtils.isNotBlank(entity.getProvincialSpecialCaseCode()) &&
            (entity.getProvincialSpecialCaseCode().equals(ProvincialSpecialCaseCodes.NOTCOMPLETED.getCode())
                || entity.getProvincialSpecialCaseCode().equals(ProvincialSpecialCaseCodes.DISQUALIFIED.getCode()));

        assessmentStudent.setWroteFlag(hasProficiencyScore || hasSpecialCaseCode ? "Y" : "N");
    }
} 