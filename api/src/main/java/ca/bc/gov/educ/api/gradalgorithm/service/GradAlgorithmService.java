package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.repository.GraduationStatusRepository;
import ca.bc.gov.educ.api.gradalgorithm.repository.StudentReportRepository;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradStudent;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GradAlgorithmService {

	private static Logger logger = LoggerFactory.getLogger(GradAlgorithmService.class);

	@Autowired
	RestTemplate restTemplate;

    @Autowired
	GraduationStatusRepository graduationStatusRepository;

    @Autowired
	StudentReportRepository studentReportRepository;

    @Autowired
	GradStudent gradStudent;

    @Autowired
	StudentCourse[] studentCourses;

	@Value("${spring.security.user.name}")
	private String username;

	@Value("${spring.security.user.password}")
	private String secret;

	@Value("${endpoint.grad-student-api.get-student-by-pen.url}")
	private String GET_GRADSTUDENT_BY_PEN_URL;

	@Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
	private String GET_STUDENT_COURSES_BY_PEN_URL;

	public GradStudent graduateStudent(String pen) {
		logger.debug("************* Graduation Algorithm START  ************\n");

		HttpHeaders httpHeaders = APIUtils.getHeaders(username, secret);

		//Get Student Demographics
		gradStudent = restTemplate.exchange(GET_GRADSTUDENT_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradStudent.class).getBody();
		logger.debug(gradStudent.getStudSurname() + ", " + gradStudent.getStudGiven());

		//Get All Courses for a Student
		studentCourses = restTemplate.exchange(GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentCourse[].class).getBody();
		logger.debug("# of courses: " + studentCourses.length);

		//TODO: Get All achievements for a Student

		//TODO: 2. Populate course achievements with course data
		//
		//        // Get Program Code for a Student and use that code to get the courses.
		//        //      OR send a list of courses from course achievements for a given student
		//        //         and retrieve the course details from course API
		//        //          (Needs a new Course API endpoint that supports this)
		//
		//        //Call course-api for all the courses in 2018 program code
		//Get all ACTIVE program rules for 2018 program
		// 2. Remove fails and duplicates
		// 3. Run Min Required Credit rules
		// 4. Read Grad Codes from COURSES
		//=======================================================================================
		// Achievements assembled above
		//=======================================================================================

		//5. Run course specific grad rules
		//6. Run Min Required Elective credit rule
		//7. Populate Report template data
		//8. Call report API

		return gradStudent;
	}
}
