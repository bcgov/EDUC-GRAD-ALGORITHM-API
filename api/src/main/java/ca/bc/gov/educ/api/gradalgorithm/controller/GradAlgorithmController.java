package ca.bc.gov.educ.api.gradalgorithm.controller;

import java.util.UUID;

import ca.bc.gov.educ.api.gradalgorithm.util.PermissionsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.service.GradAlgorithmService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@RestController
@CrossOrigin
@RequestMapping ("/api/v1")
@OpenAPIDefinition(info = @Info(title = "API for GRAD Algorithm", description = "This API is for running the grad algorithm for one or more students", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"RUN_GRAD_ALGORITHM"})})
public class GradAlgorithmController {

    private static final Logger logger = LoggerFactory.getLogger(GradAlgorithmController.class);

    @Autowired
    GradAlgorithmService gradAlgorithmService;

    @GetMapping(PermissionsConstants.RUN_GRAD_ALGORITHM)
    @PreAuthorize("#oauth2.hasScope('RUN_GRAD_ALGORITHM')")
    public GraduationData graduateStudent(@RequestParam(name = "studentID") String studentID,
                                          @RequestParam(name = "gradProgram") String gradProgram,
                                          @RequestParam(required = false) boolean projected,
                                          @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("**** GRAD ALGORITHM Started ****");
        return gradAlgorithmService.graduateStudent(UUID.fromString(studentID), gradProgram, projected, accessToken);
    }

}
