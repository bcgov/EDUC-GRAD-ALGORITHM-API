package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.struct.*;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GradAlgorithmService {

	private static Logger logger = LoggerFactory.getLogger(GradAlgorithmService.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

    @Autowired
	GradStudent gradStudent;

    @Autowired
	StudentCourse[] studentCourseArray;

    @Autowired
	StudentCourses studentCourses;

    @Autowired
	GradLetterGrades gradLetterGrades;

    @Autowired
	GradProgramSets gradProgramSets;

    @Autowired
	ProgramRules programRules;

    @Autowired
	CourseRequirements courseRequirements;

	@Value("${spring.security.user.name}")
	private String username;

	@Value("${spring.security.user.password}")
	private String secret;

	@Value("${endpoint.grad-student-api.get-student-by-pen.url}")
	private String GET_GRADSTUDENT_BY_PEN_URL;

	@Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
	private String GET_STUDENT_COURSES_BY_PEN_URL;

	public GradStudent graduateStudent(String pen) {
		logger.debug("\n************* Graduation Algorithm START  ************");

		HttpHeaders httpHeaders = APIUtils.getHeaders(username, secret);

		logger.debug("**** PEN: ****" + pen.substring(5));

		//Get Student Demographics
		gradStudent = restTemplate.exchange(GET_GRADSTUDENT_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradStudent.class).getBody();
		logger.debug(gradStudent.getStudSurname().trim() + ", " + gradStudent.getStudGiven().trim());

		int gradProgram = gradStudent.getGradRequirementYear();
		logger.debug("**** Grad Requirement Year: " + gradProgram);

		//Get All Courses for a Student
		studentCourseArray = restTemplate.exchange(GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentCourse[].class).getBody();
		logger.debug("**** # of courses: " + studentCourseArray.length);

		//Get All Program Sets for a given Grad Program
		gradProgramSets = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/programsets"
						+ "/" + gradProgram, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradProgramSets.class).getBody();

		logger.debug("**** # of Sub Programs: " + gradProgramSets.getGradProgramSetList().size());

		//Get All Program Rules for a given list of ProgramSetIDs
		List<UUID> programSetIds = new ArrayList<UUID>();
		ProgramSets programSets = new ProgramSets();

		for (GradProgramSet g : gradProgramSets.getGradProgramSetList()) {
			programSetIds.add(g.getId());
		}

		programSets.setProgramSetIDs(programSetIds);

		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(programSets);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		logger.debug("**** " + programSetIds.size() + " ProgramRuleSetIDs: " + json);

		programRules = restTemplate.exchange(
				"https://program-rule-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/program-rules/program-set", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), ProgramRules.class).getBody();
		logger.debug("**** # of Program Rules: " + programRules.getProgramRuleList().size());

		courseRequirements = restTemplate.exchange(
				"https://student-course-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/studentcourse/course-requirement", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), CourseRequirements.class).getBody();
		logger.debug("**** # of Course Requirements: " + courseRequirements.getCourseRequirements().size());

		studentCourses.setStudentCourseList(Arrays.asList(studentCourseArray.clone()));

		//Find Not Completed Courses
		try {
			json = mapper.writeValueAsString(studentCourses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		studentCourses = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/find-not-completed", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine # of Not Completed Courses: " +
				studentCourses.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isNotCompleted())
						.collect(Collectors.toList())
						.size());


		//Find Failed Courses
		try {
			json = mapper.writeValueAsString(studentCourses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		studentCourses = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/find-failed", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine # of Failed Courses: " +
				studentCourses.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isFailed())
						.collect(Collectors.toList())
						.size());

		//Find Duplicate Courses
		try {
			json = mapper.writeValueAsString(studentCourses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		studentCourses = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/find-duplicates", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine # of Duplicate Courses: " +
				studentCourses.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isDuplicate())
						.collect(Collectors.toList())
						.size());

		//Get All Grad Letter Grades
		gradLetterGrades = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/lettergrade", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradLetterGrades.class).getBody();

		logger.debug("**** # of Letter Grades: " + gradLetterGrades.getGradLetterGradeList().size());


		/*
		Boolean result = restTemplate.exchange(
				"http://localhost:8003/api/v1/rule-engine/min-credits-rule", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine Min Credits Rule: " +
				studentCourses.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isDuplicate())
						.collect(Collectors.toList())
						.result());
		 */
		logger.debug("**** Running Rule Engine Min Credits Rule");

		/*
		Boolean result = restTemplate.exchange(
				"http://localhost:8003/api/v1/rule-engine/match-credits-rules", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine Min Credits Rule: " +
				studentCourses.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isDuplicate())
						.collect(Collectors.toList())
						.result());
		 */
		logger.debug("**** Running Rule Engine Match Credits Rules");

		/*
		Boolean result = restTemplate.exchange(
				"http://localhost:8003/api/v1/rule-engine/minelective-credits-rules", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine Min Credits Rule: " +
				studentCourses.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isDuplicate())
						.collect(Collectors.toList())
						.result());
		 */
		logger.debug("**** Running Rule Engine Min Elective Credits Rules");


		//TODO: Get All achievements for a Student


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
