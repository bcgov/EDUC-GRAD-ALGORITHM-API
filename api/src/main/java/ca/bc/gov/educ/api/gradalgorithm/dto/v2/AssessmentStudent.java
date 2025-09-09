package ca.bc.gov.educ.api.gradalgorithm.dto.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentStudent extends BaseRequest {
  private String assessmentStudentID;
  private String assessmentID;
  private String schoolAtWriteSchoolID;
  private String assessmentCenterSchoolID;
  private String schoolOfRecordSchoolID;
  private String studentID;
  private String studentStatusCode;
  private String givenName;
  private String surname;
  private String pen;
  private String localID;
  private String gradeAtRegistration;
  private String proficiencyScore;
  private String assessmentFormID;
  private String adaptedAssessmentCode;
  private String irtScore;
  private String provincialSpecialCaseCode;
  private String numberOfAttempts;
  private String localAssessmentID;
  private String markingSession;
  private String courseStatusCode;
  private String downloadDate;
  private Boolean wroteFlag;
}
