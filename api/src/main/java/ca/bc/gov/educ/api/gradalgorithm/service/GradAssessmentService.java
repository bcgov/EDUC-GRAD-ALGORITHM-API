package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.Assessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentList;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentRequirements;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradalgorithm.util.APIUtils.getJSONStringFromObject;
import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_GET_ASSESSMENT_BASE_URL;
import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_GET_ASSESSMENT_REQUIREMENTS_URL;

@Service
public class GradAssessmentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradAssessmentService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    RestTemplate restTemplate;

    List<Assessment> getAllAssessments(String accessToken) {
        start();
        List<Assessment> result = webClient.get()
                .uri(GRAD_GET_ASSESSMENT_BASE_URL)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Assessment>>() {})
                .block();
        end();

        logger.info("**** # of Assessments : " + (result != null ? result.size() : 0));

        return result;
    }

    AssessmentRequirements getAllAssessmentRequirementsWithRestTemplate(
            List<StudentAssessment> studentAssessmentList, String accessToken) {
        HttpHeaders httpHeaders = APIUtils.getHeaders(accessToken);
        List<String> assessmentList = studentAssessmentList.stream()
                .map(StudentAssessment::getAssessmentCode)
                .distinct()
                .collect(Collectors.toList());

        String json = getJSONStringFromObject(new AssessmentList(assessmentList));

        AssessmentRequirements result = restTemplate.exchange(
                GRAD_GET_ASSESSMENT_REQUIREMENTS_URL,
                HttpMethod.POST, new HttpEntity<>(json, httpHeaders), AssessmentRequirements.class).getBody();
        logger.info("**** # of Assessment Requirements: " + (result != null ? result.getAssessmentRequirementList().size() : 0));

        return result;
    }

    AssessmentRequirements getAllAssessmentRequirements(List<StudentAssessment> studentAssessmentList, String accessToken) {
        List<String> assessmentList = studentAssessmentList.stream()
                .map(StudentAssessment::getAssessmentCode)
                .distinct()
                .collect(Collectors.toList());

        start();
        AssessmentRequirements result = webClient.post()
                .uri(GRAD_GET_ASSESSMENT_REQUIREMENTS_URL)
                .headers(h -> h.setBearerAuth(accessToken))
                .body(BodyInserters.fromValue(new AssessmentList(assessmentList)))
                .retrieve()
                .bodyToMono(AssessmentRequirements.class)
                .block()
                ;
        end();

        logger.info("**** # of Assessment Requirements: " + (result != null ? result.getAssessmentRequirementList().size() : 0));

        return result;
    }
}
