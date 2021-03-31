package ca.bc.gov.educ.api.gradalgorithm.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class SpecialGradAlgorithmGraduationStatus {

	private String pen;
    private UUID specialProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
}