package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_STATUS_BASE_URL;

@Service
public class GradGraduationStatusService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradGraduationStatusService.class);

    @Autowired
    private WebClient webClient;

    List<GradStudentSpecialProgram> getStudentSpecialPrograms(String pen, String accessToken) {

        start();
        List<GradStudentSpecialProgram> result = webClient.get()
                .uri(String.format(GRAD_STATUS_BASE_URL + "/specialprogram/pen/%s", pen))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradStudentSpecialProgram>>(){})
                .block();
        end();

        logger.info("**** # of Special Programs: " + (result != null ? result.size() : 0));
        return result;
    }

    GradAlgorithmGraduationStatus getStudentGraduationStatus(String studentID, String pen, String accessToken) {
        logger.debug("GET Grad Student Graduation Status: " + GradAlgorithmAPIConstants.GET_GRADSTATUS_BY_STUDENT_ID_URL + "/*****" + pen.substring(5));

        start();
        GradAlgorithmGraduationStatus result = webClient.get()
                .uri(String.format(GradAlgorithmAPIConstants.GET_GRADSTATUS_BY_STUDENT_ID_URL,studentID))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradAlgorithmGraduationStatus.class)
                .block();
        end();

        logger.debug("Grad Status: " + result.toString());

        return result;
    }

    List<GradStudentSpecialProgram> getStudentSpecialProgramsById(String studentID, String accessToken) {

        start();
        List<GradStudentSpecialProgram> result = webClient.get()
                .uri(String.format(GRAD_STATUS_BASE_URL + "/specialprogram/studentid/%s", studentID))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GradStudentSpecialProgram>>(){})
                .block();
        end();

        logger.info("**** # of Special Programs: " + (result != null ? result.size() : 0));
        return result;
    }
}
