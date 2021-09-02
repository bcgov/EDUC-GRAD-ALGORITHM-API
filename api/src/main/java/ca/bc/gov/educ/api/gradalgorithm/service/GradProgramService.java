package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.exception.GradBusinessRuleException;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradProgramService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradProgramService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private GradAlgorithmAPIConstants constants;

    
    GradProgramAlgorithmData getProgramDataForAlgorithm(String programCode,String optionalProgramCode,String accessToken) {
    	try {
	    	start();
	    	String url = constants.getProgramData() + "programCode=%s";
	    	if(StringUtils.isNotBlank(optionalProgramCode)) {
	    		url = url + "&optionalProgramCode=%s";
	    	}
	    	
	    	GradProgramAlgorithmData result = webClient.get()
	                .uri(String.format(url,programCode,optionalProgramCode))
	                .headers(h -> h.setBearerAuth(accessToken))
	                .retrieve()
	                .bodyToMono(GradProgramAlgorithmData.class)
	                .block();
	        end();
	        logger.info("**** # of Program Rules		: " + (result != null ? result.getProgramRules() != null ? result.getProgramRules().size() : 0:0));
	        logger.info("**** # of Special Program Rules: " + (result != null ? result.getOptionalProgramRules() != null ? result.getOptionalProgramRules().size() : 0:0));
	        logger.info("**** # of Program 				: " + (result != null ? result.getGradProgram() != null ? result.getGradProgram().getProgramName() :"":""));
	        return result;
    	} catch (Exception e) {
			throw new GradBusinessRuleException("GRAD-PROGRAM-API IS DOWN");
		}
    }

    UUID getSpecialProgramID(String gradProgram, String gradSpecialProgram, String accessToken) {
        try {
	    	start();
	        OptionalProgram result = webClient.get()
	                .uri(String.format(constants.getOptionalProgram(), gradProgram,gradSpecialProgram))
	                .headers(h -> h.setBearerAuth(accessToken))
	                .retrieve()
	                .bodyToMono(OptionalProgram.class)
	                .block();
	        end();
	        return result != null ? result.getOptionalProgramID() : null;
        } catch (Exception e) {
			throw new GradBusinessRuleException("GRAD-PROGRAM-API IS DOWN");
		}
    }
}
