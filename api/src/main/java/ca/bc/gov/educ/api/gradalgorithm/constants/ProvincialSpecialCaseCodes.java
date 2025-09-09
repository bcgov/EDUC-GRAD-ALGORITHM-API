package ca.bc.gov.educ.api.gradalgorithm.constants;

import lombok.Getter;

@Getter
public enum ProvincialSpecialCaseCodes {
  AEGROTAT("A"),
  EXEMPT("E"),
  DISQUALIFIED("Q"),
  NOTCOMPLETED("X");

  private final String code;
  ProvincialSpecialCaseCodes(String code) {
    this.code = code;
  }
}
