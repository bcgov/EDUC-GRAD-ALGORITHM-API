package ca.bc.gov.educ.api.gradalgorithm.controller.v2;

import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.service.GradAlgorithmService;
import ca.bc.gov.educ.api.gradalgorithm.util.PermissionsConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RestController("gradAlgorithmControllerV2")
@RequestMapping ("/api/v2")
@OpenAPIDefinition(info = @Info(title = "API for GRAD Algorithm", description = "This API is for running the grad algorithm for one or more students", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"RUN_GRAD_ALGORITHM"})})
public class GradAlgorithmController {

    GradAlgorithmService gradAlgorithmService;

    @Autowired
    public GradAlgorithmController(GradAlgorithmService gradAlgorithmService) {
        this.gradAlgorithmService = gradAlgorithmService;
    }

    @GetMapping("/graduatestudent")
    @PreAuthorize(PermissionsConstants.RUN_GRAD_ALGORITHM)
    public GraduationData graduateStudent(@RequestParam(name = "studentID") String studentID,
                                          @RequestParam(name = "gradProgram") String gradProgram,
                                          @RequestParam(required = false) boolean projected,
                                          @RequestParam(required = false, defaultValue = "") String hypotheticalGradYear) {
        log.debug("**** GRAD ALGORITHM Started ****");
        return gradAlgorithmService.graduateStudent(UUID.fromString(studentID), gradProgram, projected, hypotheticalGradYear, true);
    }

}
