package ca.bc.gov.educ.api.gradalgorithm.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradStudentSpecialProgram {

	private UUID id;
    private String pen;
    private UUID specialProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
    private String specialProgramName;
    private String specialProgramCode;
    private String mainProgramCode;
    private UUID studentID;
				
}
