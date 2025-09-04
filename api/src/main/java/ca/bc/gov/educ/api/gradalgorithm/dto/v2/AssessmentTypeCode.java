package ca.bc.gov.educ.api.gradalgorithm.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@SuppressWarnings("squid:S1700")
public class AssessmentTypeCode extends BaseRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String assessmentTypeCode;
  private String label;
  private Integer displayOrder;
  private String language;
  private String effectiveDate;
  private String expiryDate;
}
