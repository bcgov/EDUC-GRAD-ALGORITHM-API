package ca.bc.gov.educ.api.gradalgorithm.endpoint;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.bc.gov.educ.api.gradalgorithm.struct.GraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.util.PermissionsContants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RequestMapping("/api/v1")
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Assessment Data.", description = "This Read API is for Reading Assessment data.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_ASSESSMENT_DATA","READ_GRAD_ASSESSMENT_REQUIREMENT_DATA"})})
public interface GradAlgorithmEndpoint {

    @GetMapping("/graduatestudent/{pen}")
    @PreAuthorize(PermissionsContants.RUN_GRAD_ALGORITHM)
    public GraduationStatus graduateStudent(@PathVariable String pen);

    //@PostMapping("/graduate-students")
    //public List<GradStudent> graduateStudents(@RequestParam List<String> penList);
}
