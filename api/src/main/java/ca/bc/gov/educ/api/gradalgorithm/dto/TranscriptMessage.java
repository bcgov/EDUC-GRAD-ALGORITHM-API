package ca.bc.gov.educ.api.gradalgorithm.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@Component
public class TranscriptMessage {

	private UUID transcriptMessageID; 
	private String programCode; 
	private String messageTypeCode;
	private String gradMainMessage;
	private String gradDateMessage;
	private String honourNote;
	private String adIBProgramMessage;
	private String frenchImmersionMessage;
	private String careerProgramMessage;
	private String gradProjectedMessage;
	private String honourProjectedNote;
	private String graduationSchool;
}