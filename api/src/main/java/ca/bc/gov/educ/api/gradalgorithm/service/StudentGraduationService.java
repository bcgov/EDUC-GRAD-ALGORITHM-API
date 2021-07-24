package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class StudentGraduationService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(StudentGraduationService.class);

    @Autowired
    private WebClient webClient;
    
    @Autowired
    private GradAlgorithmAPIConstants constants;

    StudentGraduationAlgorithmData getAllAlgorithmData(String programCode,String accessToken) {
        start();
        StudentGraduationAlgorithmData result = webClient.get()
                .uri(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata/"+programCode)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(StudentGraduationAlgorithmData.class)
                .block();
        end();

        logger.info("**** # of Letter Grades  : " + (result != null ? result.getLetterGrade().size() : 0));
        logger.info("**** # of Special Cases  : " + (result != null ? result.getSpecialCase().size() : 0));
        logger.info("**** # of Algorithm Rules: " + (result != null ? result.getProgramAlgorithmRules().size() : 0));
        return result;
    }
    
    TranscriptMessage getGradMessages(String gradProgram, String msgType, String accessToken) {

        start();
        TranscriptMessage result = webClient.get()
                .uri(String.format(constants.getGraduationMessage(), gradProgram, msgType))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(TranscriptMessage.class)
                .block();
        end();

        return result;
    }
    
}
