package ca.bc.gov.educ.api.gradalgorithm.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ExceptionMessage {

	private String exceptionName;
	private String exceptionDetails;
}
