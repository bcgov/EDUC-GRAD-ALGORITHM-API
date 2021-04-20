package ca.bc.gov.educ.api.gradalgorithm.controller;

import ca.bc.gov.educ.api.gradalgorithm.struct.GraduationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.gradalgorithm.endpoint.GradAlgorithmEndpoint;
import ca.bc.gov.educ.api.gradalgorithm.service.GradAlgorithmService;
import ca.bc.gov.educ.api.gradalgorithm.struct.RuleProcessorData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@EnableResourceServer
public class GradAlgorithmController implements GradAlgorithmEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(GradAlgorithmController.class);

    @Autowired
    GradAlgorithmService gradAlgorithmService;

    public GraduationData graduateStudent(String pen, String gradProgram, boolean projected){
        logger.debug("**** GRAD ALGORITHM Started ****");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String accessToken = details.getTokenValue();
        return gradAlgorithmService.graduateStudent(pen, gradProgram, projected, accessToken);
    }

}
