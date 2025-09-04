package ca.bc.gov.educ.api.gradalgorithm.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseRequest {
  protected String createUser;
  protected String updateUser;
  protected String createDate;
  protected String updateDate;
}
