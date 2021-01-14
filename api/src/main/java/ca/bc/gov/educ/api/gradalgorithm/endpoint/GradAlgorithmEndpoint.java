package ca.bc.gov.educ.api.gradalgorithm.endpoint;

import ca.bc.gov.educ.api.gradalgorithm.struct.GraduationData;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequestMapping ("/api/v1")
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for GRAD Algorithm", description = "This API is for running the grad algorithm for one or more students", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"RUN_GRAD_ALGORITHM"})})
public interface GradAlgorithmEndpoint {

    @GetMapping("/graduatestudent")
    @PreAuthorize("#oauth2.hasScope('RUN_GRAD_ALGORITHM')")
    public GraduationData graduateStudent(@RequestParam(name = "pen") String pen,
                                          @RequestParam(name = "gradProgram") String gradProgram);

    //@PostMapping("/graduate-students")
    //public List<GradStudent> graduateStudents(@RequestParam List<String> penList);
}
