package ca.bc.gov.educ.api.gradalgorithm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradSchool {

    private String schoolID;
    private String canIssueTranscripts;
    private String canIssueCertificates;

}