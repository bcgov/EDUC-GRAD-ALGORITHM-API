package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.gradalgorithm.struct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;

@Service
public class GradAlgorithmService {

	private static Logger logger = LoggerFactory.getLogger(GradAlgorithmService.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	GraduationData graduationData;

    @Autowired
	GradStudent gradStudent;

    @Autowired
	StudentCourse[] studentCourseArray;

    @Autowired
	StudentCourses studentCourses;

    @Autowired
	StudentAssessments studentAssessments;

    @Autowired
	StudentExams studentExams;

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

	public GraduationData graduateStudent(String pen, String gradProgram, String accessToken) {
		logger.debug("\n************* Graduation Algorithm START  ************");

		httpHeaders = APIUtils.getHeaders(accessToken);

		logger.debug("**** PEN: ****" + pen.substring(5));

		//Get Student Demographics
		gradStudent = getStudentDemographics(pen);
		graduationData.setGradStudent(gradStudent);

		logger.debug("**** Grad Program: " + gradProgram);

		//Get All Courses for a Student
		studentCourseArray = getAllCoursesForAStudent(pen);

		//Get All Assessments for a Student
		studentAssessments = getAllAssessmentsForAStudent(pen);
		graduationData.setStudentAssessments(studentAssessments);

		//Get All Exams for a Student
		studentExams = getAllExamsForAStudent(pen);
		graduationData.setStudentExams(studentExams);

		//Get All Program Sets for a given Grad Program
		gradProgramSets = getProgramSets(gradProgram);

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
		gradStatus.setProgram(gradStudent.getGradRequirementYear() + "");
		gradStatus.setProgramCompletionDate(null);//setting to null till the logic is implemented
		gradStatus.setGpa("0.0000");
		gradStatus.setHonoursFlag("U");
		gradStatus.setSchoolOfRecord(gradStudent.getMincode());
		gradStatus.setStudentGrade("TBD");

		graduationData.setGradStatus(gradStatus);
		graduationData.setGraduated(isGraduated);
		graduationData.setSchool(getSchool(gradStudent.getMincode()));

		List<GradRequirement> reqMet = new ArrayList<>();
		List<GradRequirement> reqNotMet = new ArrayList<>();

		reqMet = matchRuleData.getPassMessages();
		reqNotMet  = matchRuleData.getFailMessages();

		if (!minCreditRuleData.isPassed()) {
			reqNotMet.add(new GradRequirement(
					minCreditRuleData.getProgramRule().getCode(),
					minCreditRuleData.getProgramRule().getNotMetDescription()));
		}

		if (!minElectiveCreditRuleData.isPassed()) {
			reqNotMet.add(new GradRequirement(
					minElectiveCreditRuleData.getProgramRule().getCode(),
					minElectiveCreditRuleData.getProgramRule().getNotMetDescription()));
		}

		graduationData.setRequirementsMet(reqMet);
		graduationData.setNonGradReasons(reqNotMet);

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

	private StudentAssessments getAllAssessmentsForAStudent(String pen) {
		StudentAssessment[] result = restTemplate.exchange(
				"https://student-assessment-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/studentassessment/pen"
						+ "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentAssessment[].class).getBody();
		logger.debug("**** # of Assessments: " + result.length);

		this.studentAssessments.setStudentAssessmentList(Arrays.asList(result.clone()));

		return studentAssessments;
	}

	private StudentExams getAllExamsForAStudent(String pen) {
		StudentExam[] result = restTemplate.exchange(
				"https://student-exam-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/studentexam/pen"
						+ "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentExam[].class).getBody();
		logger.debug("**** # of Exams: " + result.length);

		this.studentExams.setStudentExamList(Arrays.asList(result.clone()));

		return studentExams;
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
		String json = getJSONStringFromObject(studentCourses);

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
		String json = getJSONStringFromObject(studentCourses);

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
		String json = getJSONStringFromObject(studentCourses);

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
		String json = getJSONStringFromObject(minCreditRuleData);

		logger.debug("**** Running Rule Engine Min Credits Rule");

		MinCreditRuleData result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/run-mincredits", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MinCreditRuleData.class).getBody();

		return result;
	}

	private MatchRuleData runMatchRules(ProgramRules matchRules, StudentCourses uniqueStudentCourses, CourseRequirements courseRequirements) {
		MatchRuleData matchRuleData = new MatchRuleData(matchRules, uniqueStudentCourses, courseRequirements);
		String json = getJSONStringFromObject(matchRuleData);

		logger.debug("**** Running Rule Engine Match Rules");

		MatchRuleData result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/run-matchrules", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MatchRuleData.class).getBody();

		return result;
	}

	private MinElectiveCreditRuleData hasMinElectiveCredits(ProgramRule minElectiveCreditRule, StudentCourses uniqueStudentCourses){
		MinElectiveCreditRuleData minElectiveCreditRuleData = new MinElectiveCreditRuleData(minElectiveCreditRule,
				uniqueStudentCourses, 0, 0, false);
		String json = getJSONStringFromObject(minElectiveCreditRuleData);

		logger.debug("**** Running Rule Engine Min Elective Credits Rule");

		MinElectiveCreditRuleData result = restTemplate.exchange(
				"https://rule-engine-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/rule-engine/run-minelectivecredits", HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MinElectiveCreditRuleData.class).getBody();

		return result;
	}

	private School getSchool(String minCode){

		School result = restTemplate.exchange(
				"https://educ-grad-school-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/school" + "/" + minCode, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), School.class).getBody();

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
