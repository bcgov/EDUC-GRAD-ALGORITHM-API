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
	GraduationData graduationData;

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

	@Value("${endpoint.grad-student-api.get-student-by-pen.url}")
	private String GET_GRADSTUDENT_BY_PEN_URL;

	@Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
	private String GET_STUDENT_COURSES_BY_PEN_URL;

	boolean isGraduated = true;

	HttpHeaders httpHeaders;

	public GraduationData graduateStudent(String pen, String accessToken) {
		logger.debug("\n************* Graduation Algorithm START  ************");

		httpHeaders = APIUtils.getHeaders(accessToken);

		logger.debug("**** PEN: ****" + pen.substring(5));

		//Get Student Demographics
		gradStudent = getStudentDemographics(pen);
		graduationData.setGradStudent(gradStudent);

		logger.debug("**** Grad Requirement Year: " + gradStudent.getGradRequirementYear());

		//Get All Courses for a Student
		studentCourseArray = getAllCoursesForAStudent(pen);

		//Get All Program Sets for a given Grad Program
		gradProgramSets = getProgramSets("" + gradStudent.getGradRequirementYear());

		//Get All Program Rules for a given list of ProgramSetIDs
		programRules = getProgramRules(gradProgramSets);

		//Get All course Requirements
		courseRequirements = getAllCourseRequirements();

		studentCourses.setStudentCourseList(Arrays.asList(studentCourseArray.clone()));

		//Find Not Completed Courses
		studentCourses = processCoursesForNotCompleted(studentCourses);

		//Find Failed Courses
		studentCourses = processCoursesForFailed(studentCourses);

		//Find Duplicate Courses
		studentCourses = processCoursesForDuplicates(studentCourses);

		//Get All Grad Letter Grades
		//gradLetterGrades = getAllLetterGrades();

		//Get Unique student courses
		StudentCourses uniqueStudentCourses = getUniqueStudentCourses(studentCourses);

		//Run Min Credits Rule
		ProgramRule minCreditRule = programRules.getProgramRuleList()
				.stream()
				.filter(pr -> pr.getRequirementType().compareTo("MC") == 0)
				.collect(Collectors.toList()).get(0);

		//logger.debug("Unique Student Courses:\n:" + uniqueStudentCourses.toString());
		MinCreditRuleData minCreditRuleData = hasMinCredits(minCreditRule, uniqueStudentCourses);
		if (minCreditRuleData.isPassed()) {
			logger.debug("Min Credits rule Passed! - Required: "
					+ minCreditRuleData.getRequiredCredits() + " Has: " + minCreditRuleData.getAcquiredCredits());
		}
		else {
			logger.debug("Min Credits rule Failed! - Required: "
					+ minCreditRuleData.getRequiredCredits() + " Has: " + minCreditRuleData.getAcquiredCredits());
			isGraduated = false;
		}

		uniqueStudentCourses = minCreditRuleData.getStudentCourses();

		//Run Match Credits rules
		List<ProgramRule> matchRulesList = programRules.getProgramRuleList()
				.stream()
				.filter(pr -> pr.getRequirementType().compareTo("M") == 0)
				.collect(Collectors.toList());

		//logger.debug("Unique Student Courses:\n:" + uniqueStudentCourses.toString());
		MatchRuleData matchRuleData = runMatchRules(new ProgramRules(matchRulesList), uniqueStudentCourses, courseRequirements);
		if (matchRuleData.isPassed()) {
			logger.debug("All Match rules Passed!");
		}
		else {
			logger.debug("One or More Match rules Failed!");
			isGraduated = false;
		}

		//Run Min Elective Credits rule
		ProgramRule minElectiveCreditsRule = programRules.getProgramRuleList()
				.stream()
				.filter(pr -> pr.getRequirementType().compareTo("MCE") == 0)
				.collect(Collectors.toList()).get(0);

		//logger.debug("Unique Student Courses:\n:" + uniqueStudentCourses.toString());
		MinElectiveCreditRuleData minElectiveCreditRuleData = hasMinElectiveCredits(minElectiveCreditsRule, matchRuleData.getStudentCourses());
		if (minElectiveCreditRuleData.isPassed()) {
			logger.debug("Min Elective Credits rule Passed! - Required: "
					+ minElectiveCreditRuleData.getRequiredCredits() + " Has: " + minElectiveCreditRuleData.getAcquiredCredits());
		}
		else {
			logger.debug("Min Elective Credits rule Failed! - Required: "
					+ minElectiveCreditRuleData.getRequiredCredits() + " Has: " + minElectiveCreditRuleData.getAcquiredCredits());
			isGraduated = false;
		}

		graduationData.setStudentCourses(minElectiveCreditRuleData.getStudentCourses());

		//Populate Grad Status Details
		GradAlgorithmGraduationStatus gradStatus = new GradAlgorithmGraduationStatus();
		gradStatus.setPen(pen);
		gradStatus.setGradProgram(gradStudent.getGradRequirementYear() + "");
		gradStatus.setGraduationDate(null);//setting to null till the logic is implemented
		gradStatus.setStudentGradeAtGraduation("TBD");
		gradStatus.setGpa("0.0000");
		gradStatus.setHonoursFlag("U");
		gradStatus.setSchoolOfRecord(gradStudent.getMincode());
		gradStatus.setStudentGrade("TBD");

		graduationData.setGradStatus(gradStatus);

		graduationData.setGraduated(isGraduated);

		return graduationData;
	}

	/*
	********************************************************************************************************************
	Utility Methods
	********************************************************************************************************************
	 */
	private GradStudent getStudentDemographics(String pen) {
		GradStudent result = restTemplate.exchange(GET_GRADSTUDENT_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradStudent.class).getBody();

		logger.debug(result.getStudSurname().trim() + ", " + result.getStudGiven().trim());

		return result;
	}

	private StudentCourse[] getAllCoursesForAStudent(String pen) {
		StudentCourse[] result = restTemplate.exchange(GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentCourse[].class).getBody();
		logger.debug("**** # of courses: " + result.length);

		return result;
	}

	private GradProgramSets getProgramSets(String gradProgram) {
		GradProgramSets result = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/programsets"
						+ "/" + gradProgram, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradProgramSets.class).getBody();

		logger.debug("**** # of Sub Programs: " + result.getGradProgramSetList().size());

		return result;
	}

	private ProgramRules getProgramRules(GradProgramSets gradProgramSets) {
		List<UUID> programSetIds = new ArrayList<UUID>();
		ProgramSets programSets = new ProgramSets();

		for (GradProgramSet g : gradProgramSets.getGradProgramSetList()) {
			programSetIds.add(g.getId());
		}

		programSets.setProgramSetIDs(programSetIds);

		String json = getJSONStringFromObject(programSets);

		logger.debug("**** " + programSetIds.size() + " ProgramRuleSetIDs: " + json);

		ProgramRules result = restTemplate.exchange(
				"https://program-rule-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/program-rules/program-set", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), ProgramRules.class).getBody();
		logger.debug("**** # of Program Rules: " + result.getProgramRuleList().size());

		return result;
	}

	private CourseRequirements getAllCourseRequirements() {
		CourseRequirements result = restTemplate.exchange(
				"https://grad-course-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/course/course-requirement", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), CourseRequirements.class).getBody();
		logger.debug("**** # of Course Requirements: " + result.getCourseRequirementList().size());

		return result;
	}

	private StudentCourses processCoursesForNotCompleted(StudentCourses studentCourses) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(studentCourses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		StudentCourses result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/find-not-completed", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine # of Not Completed Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isNotCompleted())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private StudentCourses processCoursesForFailed(StudentCourses studentCourses) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(studentCourses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		StudentCourses result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/find-failed", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine # of Failed Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isFailed())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private StudentCourses processCoursesForDuplicates(StudentCourses studentCourses) {
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(studentCourses);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		StudentCourses result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/find-duplicates", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.debug("**** Rule Engine # of Duplicate Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isDuplicate())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private GradLetterGrades getAllLetterGrades(){
		GradLetterGrades result = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/lettergrade", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradLetterGrades.class).getBody();
		logger.debug("**** # of Letter Grades: " + result.getGradLetterGradeList().size());

		return result;
	}

	private StudentCourses getUniqueStudentCourses(StudentCourses studentCourses){
		List<StudentCourse> uniqueStudentCourseList = new ArrayList<StudentCourse>();

		uniqueStudentCourseList = studentCourses.getStudentCourseList()
				.stream()
				.filter(sc -> !sc.isNotCompleted())
				.collect(Collectors.toList())
				.stream()
				.filter(sc -> !sc.isDuplicate())
				.collect(Collectors.toList())
				.stream()
				.filter(sc -> !sc.isFailed())
				.collect(Collectors.toList());

		StudentCourses result = new StudentCourses();
		result.setStudentCourseList(uniqueStudentCourseList);

		return result;
	}

	private MinCreditRuleData hasMinCredits(ProgramRule minCreditRule, StudentCourses uniqueStudentCourses){
		MinCreditRuleData minCreditRuleData = new MinCreditRuleData(minCreditRule, uniqueStudentCourses,
				0, 0, false);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(minCreditRuleData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		logger.debug("**** Running Rule Engine Min Credits Rule");
		//logger.debug(json);

		MinCreditRuleData result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/run-mincredits", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MinCreditRuleData.class).getBody();
		//logger.debug("**** Min Credits Rule passed?: " + result);

		return result;
	}

	private MatchRuleData runMatchRules(ProgramRules matchRules, StudentCourses uniqueStudentCourses, CourseRequirements courseRequirements) {
		MatchRuleData matchRuleData = new MatchRuleData(matchRules, uniqueStudentCourses, courseRequirements);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(matchRuleData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		logger.debug("**** Running Rule Engine Match Rules");
		//logger.debug(json);

		MatchRuleData result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/run-matchrules", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MatchRuleData.class).getBody();
		//logger.debug("**** All Match Rules passed?: " + result);

		return result;
	}

	private MinElectiveCreditRuleData hasMinElectiveCredits(ProgramRule minElectiveCreditRule, StudentCourses uniqueStudentCourses){
		MinElectiveCreditRuleData minElectiveCreditRuleData = new MinElectiveCreditRuleData(minElectiveCreditRule,
				uniqueStudentCourses, 0, 0, false);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(minElectiveCreditRuleData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		logger.debug("**** Running Rule Engine Min Elective Credits Rule");
		//logger.debug(json);

		MinElectiveCreditRuleData result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/run-minelectivecredits", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MinElectiveCreditRuleData.class).getBody();
		//logger.debug("**** Min Elective Credits Rule passed?: " + result);

		return result;
	}

	private <T> String getJSONStringFromObject(T inputObject){
		ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(inputObject);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return json;
	}
}
