package ca.bc.gov.educ.api.gradalgorithm.endpoint;

import ca.bc.gov.educ.api.gradalgorithm.struct.GraduationData;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1")
public interface GradAlgorithmEndpoint {

    //@GetMapping("/graduatestudent/{pen}")
    //public GradStudent graduateStudent(@PathVariable String pen);

    @GetMapping("/graduatestudent")
    //@PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_COURSE_DATA')")
    public GraduationData graduateStudent(@RequestParam(name = "pen") String pen,
                                          @RequestParam(name = "gradProgram") String gradProgram);

    //@PostMapping("/graduate-students")
    //public List<GradStudent> graduateStudents(@RequestParam List<String> penList);
}
