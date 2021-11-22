package ca.bc.gov.educ.api.gradalgorithm.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

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
	private String programCadre;
	private String careerProgramMessage;
	private String gradProjectedMessage;
	private String honourProjectedNote;
}