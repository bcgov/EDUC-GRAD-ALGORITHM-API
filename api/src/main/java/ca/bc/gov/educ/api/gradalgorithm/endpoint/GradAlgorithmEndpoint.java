package ca.bc.gov.educ.api.gradalgorithm.endpoint;

import ca.bc.gov.educ.api.gradalgorithm.struct.GradStudent;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1")
public interface GradAlgorithmEndpoint {

    @GetMapping("/graduate-student/{pen}")
    public GradStudent graduateStudent(@PathVariable String pen);

    @PostMapping("/graduate-students")
    public List<GradStudent> graduateStudents(@RequestParam List<String> penList);
}
