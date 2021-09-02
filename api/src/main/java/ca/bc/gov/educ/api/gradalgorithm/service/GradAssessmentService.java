package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.exception.GradBusinessRuleException;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradAssessmentService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradAssessmentService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private GradAlgorithmAPIConstants constants;
    
    AssessmentAlgorithmData getAssessmentDataForAlgorithm(String pen,String accessToken) {
    	try 
    	{
	    	start();
	    	AssessmentAlgorithmData result = webClient.get()
	                .uri(String.format(constants.getAssessmentData(),pen))
	                .headers(h -> h.setBearerAuth(accessToken))
	                .retrieve()
	                .bodyToMono(AssessmentAlgorithmData.class)
	                .block();
	        end();
	        
	        if(!result.getStudentAssessments().isEmpty()) {
		        for (StudentAssessment studentAssessment : result.getStudentAssessments()) {
		            studentAssessment.setGradReqMet("");
		            studentAssessment.setGradReqMetDetail("");
		        }
	        }
	        logger.info("**** # of Student Assessments: " + (result.getStudentAssessments() != null ? result.getStudentAssessments().size() : 0));
	        logger.info("**** # of Assessment Requirements: " + (result.getAssessmentRequirements() != null ? result.getAssessmentRequirements().size() : 0));
	        logger.info("**** # of Assessments: " + (result.getAssessments() != null ? result.getAssessments().size() : 0));
	        return result;
    	} catch (Exception e) {
			throw new GradBusinessRuleException("GRAD-ASSESSMENT-API IS DOWN");
		}
    }
}
