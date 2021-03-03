package ca.bc.gov.educ.api.gradalgorithm.service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.gradalgorithm.struct.*;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
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
	CourseRequirements courseRequirements;

	boolean isGraduated = true;

	HttpHeaders httpHeaders;

	public GraduationData graduateStudent(String pen, String gradProgram, boolean projected, String accessToken) {
		logger.info("\n************* Graduation Algorithm START  ************");

		httpHeaders = APIUtils.getHeaders(accessToken);

		logger.info("**** PEN: ****" + pen.substring(5));

		//Get Student Demographics
		gradStudent = getStudentDemographics(pen);
		graduationData.setGradStudent(gradStudent);

		logger.info("**** Grad Program: " + gradProgram);

		//Get All Courses for a Student
		studentCourseArray = getAllCoursesForAStudent(pen);

		//Get All Assessments for a Student
		studentAssessments = getAllAssessmentsForAStudent(pen);
		graduationData.setStudentAssessments(studentAssessments);

		//Get All Exams for a Student
		studentExams = getAllExamsForAStudent(pen);
		graduationData.setStudentExams(studentExams);

		//Get All Program Sets for a given Grad Program
		//gradProgramSets = getProgramSets(gradProgram);

		//Get All Program Rules for a given list of ProgramSetIDs
		//gradProgramRules = getProgramRules(gradProgram, null);

		//Get All course Requirements
		courseRequirements = getAllCourseRequirements();

		studentCourses.setStudentCourseList(Arrays.asList(studentCourseArray.clone()));

		//Find Not Completed Courses
		studentCourses = processCoursesForNotCompleted(studentCourses);

		//Find Registered Courses only if the flag is Y
		studentCourses = processCoursesForProjected(studentCourses);

		//Find Failed Courses
		studentCourses = processCoursesForFailed(studentCourses);

		//Find Duplicate Courses
		studentCourses = processCoursesForDuplicates(studentCourses);

		//Find Career Preparation Courses
		studentCourses = processCoursesForCP(studentCourses);

		//Find Locally Developed Courses
		studentCourses = processCoursesForLD(studentCourses);

		//Get All Grad Letter Grades
		gradLetterGrades = getAllLetterGrades();

		//Get Unique student courses
		StudentCourses uniqueStudentCourses = getUniqueStudentCourses(studentCourses, projected);

		//Run Min Credits Rule
		GradProgramRule minCreditRule = getProgramRules(gradProgram, "MC").get(0);

		//logger.debug("Unique Student Courses:\n:" + uniqueStudentCourses.toString());
		MinCreditRuleData minCreditRuleData = hasMinCredits(minCreditRule, uniqueStudentCourses);
		if (minCreditRuleData.isPassed()) {
			logger.info("Min Credits rule Passed! - Required: "
					+ minCreditRuleData.getRequiredCredits() + " Has: " + minCreditRuleData.getAcquiredCredits());
		}
		else {
			logger.info("Min Credits rule Failed! - Required: "
					+ minCreditRuleData.getRequiredCredits() + " Has: " + minCreditRuleData.getAcquiredCredits());
			isGraduated = false;
		}

		uniqueStudentCourses = minCreditRuleData.getStudentCourses();

		//Run Match Credits rules
		List<GradProgramRule> matchRulesList = getProgramRules(gradProgram, "M");

		//logger.debug("Unique Student Courses:\n:" + uniqueStudentCourses.toString());
		MatchRuleData matchRuleData = runMatchRules(new GradProgramRules(matchRulesList), uniqueStudentCourses, courseRequirements);
		if (matchRuleData.isPassed()) {
			logger.info("All Match rules Passed!");
		}
		else {
			logger.info("One or More Match rules Failed!");
			isGraduated = false;
		}

		//Run Min Elective Credits rule
		GradProgramRule minElectiveCreditsRule = getProgramRules(gradProgram, "MCE").get(0);

		//logger.debug("Unique Student Courses:\n:" + uniqueStudentCourses.toString());
		MinElectiveCreditRuleData minElectiveCreditRuleData = hasMinElectiveCredits(minElectiveCreditsRule, matchRuleData.getStudentCourses());
		if (minElectiveCreditRuleData.isPassed()) {
			logger.info("Min Elective Credits rule Passed! - Required: "
					+ minElectiveCreditRuleData.getRequiredCredits() + " Has: " + minElectiveCreditRuleData.getAcquiredCredits());
		}
		else {
			logger.info("Min Elective Credits rule Failed! - Required: "
					+ minElectiveCreditRuleData.getRequiredCredits() + " Has: " + minElectiveCreditRuleData.getAcquiredCredits());
			isGraduated = false;
		}

		graduationData.setStudentCourses(minElectiveCreditRuleData.getStudentCourses());

		//Populate Grad Status Details
		GradAlgorithmGraduationStatus gradStatus = new GradAlgorithmGraduationStatus();
		gradStatus.setPen(pen);
		gradStatus.setProgram(gradProgram);
		if (isGraduated) {
			gradStatus.setProgramCompletionDate(getGradDate(graduationData.getStudentCourses().getStudentCourseList(),
					graduationData.getStudentAssessments().getStudentAssessmentList()));
		}
		gradStatus.setGpa(getGPA(graduationData.getStudentCourses().getStudentCourseList(),
				graduationData.getStudentAssessments().getStudentAssessmentList(),
				gradLetterGrades.getGradLetterGradeList()));
		gradStatus.setHonoursFlag(getHonoursFlag(gradStatus.getGpa()));
		gradStatus.setSchoolOfRecord(gradStudent.getMincode());

		graduationData.setGradStatus(gradStatus);
		graduationData.setGraduated(isGraduated);
		graduationData.setSchool(getSchool(gradStudent.getMincode()));

		List<GradRequirement> reqMet = new ArrayList<>();
		List<GradRequirement> reqNotMet = new ArrayList<>();

		reqMet = matchRuleData.getPassMessages();
		reqNotMet  = matchRuleData.getFailMessages();

		if (minCreditRuleData.isPassed()) {
			reqMet.add(new GradRequirement(
					minCreditRuleData.getGradProgramRule().getRuleCode(),
					minCreditRuleData.getGradProgramRule().getRequirementName()));
		}
		else {
			reqNotMet.add(new GradRequirement(
					minCreditRuleData.getGradProgramRule().getRuleCode(),
					minCreditRuleData.getGradProgramRule().getNotMetDesc()));
		}

		if (minElectiveCreditRuleData.isPassed()) {
			reqMet.add(new GradRequirement(
					minElectiveCreditRuleData.getGradProgramRule().getRuleCode(),
					minElectiveCreditRuleData.getGradProgramRule().getRequirementName()));
		}
		else {
			reqNotMet.add(new GradRequirement(
					minElectiveCreditRuleData.getGradProgramRule().getRuleCode(),
					minElectiveCreditRuleData.getGradProgramRule().getNotMetDesc()));
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
		logger.debug("GET Grad Student Demographics: " + GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/" + pen);
		GradStudent result = restTemplate.exchange(
				GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradStudent.class).getBody();

		logger.debug(result.getStudSurname().trim() + ", " + result.getStudGiven().trim());

		return result;
	}

	private StudentCourse[] getAllCoursesForAStudent(String pen) {
		StudentCourse[] result = restTemplate.exchange(
				GradAlgorithmAPIConstants.GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentCourse[].class).getBody();
		logger.info("**** # of courses: " + result.length);

		for (int i = 0; i < result.length; i++) {
			result[i].setGradReqMet("");
			result[i].setGradReqMetDetail("");
		}

		return result;
	}

	private StudentAssessments getAllAssessmentsForAStudent(String pen) {
		StudentAssessment[] result = restTemplate.exchange(
				"https://student-assessment-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/studentassessment/pen"
						+ "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentAssessment[].class).getBody();
		logger.info("**** # of Assessments: " + result.length);

		this.studentAssessments.setStudentAssessmentList(Arrays.asList(result.clone()));

		return studentAssessments;
	}

	private StudentExams getAllExamsForAStudent(String pen) {
		StudentExam[] result = restTemplate.exchange(
				"https://student-exam-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/studentexam/pen"
						+ "/" + pen, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), StudentExam[].class).getBody();
		logger.info("**** # of Exams: " + result.length);

		this.studentExams.setStudentExamList(Arrays.asList(result.clone()));

		return studentExams;
	}

	private GradProgramSets getProgramSets(String gradProgram) {
		GradProgramSets result = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/programsets"
						+ "/" + gradProgram, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradProgramSets.class).getBody();

		logger.info("**** # of Sub Programs: " + result.getGradProgramSetList().size());

		return result;
	}

	/*private ProgramRules getProgramRules(GradProgramSets gradProgramSets) {
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
	}*/

	private List<GradProgramRule> getProgramRules(String programCode, String requirementType) {
		List<GradProgramRule> result = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/" +
						"programrules?programCode=" + programCode + "&requirementType=" + requirementType, HttpMethod.GET,
				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradProgramRule>>() {}).getBody();
		logger.info("**** # of Program Rules: " + result.size());

		return result;
	}

	private CourseRequirements getAllCourseRequirements() {
		CourseRequirements result = restTemplate.exchange(
				"https://grad-course-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/course/course-requirement", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), CourseRequirements.class).getBody();
		logger.info("**** # of Course Requirements: " + result.getCourseRequirementList().size());

		return result;
	}

	private StudentCourses processCoursesForNotCompleted(StudentCourses studentCourses) {
		String json = getJSONStringFromObject(studentCourses);

		StudentCourses result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_FIND_NOT_COMPLETED, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.info("**** Rule Engine # of Not Completed Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isNotCompleted())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private StudentCourses processCoursesForProjected(StudentCourses studentCourses) {
		String json = getJSONStringFromObject(studentCourses);

		StudentCourses result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
					+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_FIND_PROJECTED, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.info("**** Rule Engine # of Projected Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isProjected())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private StudentCourses processCoursesForFailed(StudentCourses studentCourses) {
		String json = getJSONStringFromObject(studentCourses);

		StudentCourses result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_FIND_FAILED, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.info("**** Rule Engine # of Failed Courses: " +
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
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_FIND_DUPLICATES, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.info("**** Rule Engine # of Duplicate Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isDuplicate())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private StudentCourses processCoursesForCP(StudentCourses studentCourses) {
		String json = getJSONStringFromObject(studentCourses);

		StudentCourses result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_FIND_CP, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.info("**** Rule Engine # of Career Program Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isCareerPrep())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private StudentCourses processCoursesForLD(StudentCourses studentCourses) {
		String json = getJSONStringFromObject(studentCourses);

		StudentCourses result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_FIND_LD, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), StudentCourses.class).getBody();

		logger.info("**** Rule Engine # of Locally Developed Courses: " +
				result.getStudentCourseList()
						.stream()
						.filter(sc -> sc.isLocallyDeveloped())
						.collect(Collectors.toList())
						.size());

		return result;
	}

	private GradLetterGrades getAllLetterGrades(){
		GradLetterGrades result = restTemplate.exchange(
				"https://educ-grad-program-management-api-wbmfsf-dev.pathfinder.gov.bc.ca/api/v1/programmanagement/lettergrade", HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GradLetterGrades.class).getBody();
		logger.info("**** # of Letter Grades: " + result.getGradLetterGradeList().size());

		return result;
	}

	private StudentCourses getUniqueStudentCourses(StudentCourses studentCourses, boolean projected){
		List<StudentCourse> uniqueStudentCourseList = new ArrayList<StudentCourse>();

		uniqueStudentCourseList = studentCourses.getStudentCourseList()
				.stream()
				.filter(sc -> !sc.isNotCompleted()
							&& !sc.isDuplicate()
							&& !sc.isFailed()
							&& !sc.isCareerPrep()
							&& !sc.isLocallyDeveloped())
				.collect(Collectors.toList());

		if (!projected) {
			logger.info("Excluding Registrations!");
			uniqueStudentCourseList = uniqueStudentCourseList
					.stream()
					.filter(sc -> !sc.isProjected())
					.collect(Collectors.toList());
		}
		else
			logger.info("Including Registrations!");

		StudentCourses result = new StudentCourses();
		result.setStudentCourseList(uniqueStudentCourseList);

		return result;
	}

	private MinCreditRuleData hasMinCredits(GradProgramRule minCreditRule, StudentCourses uniqueStudentCourses){
		MinCreditRuleData minCreditRuleData = new MinCreditRuleData(minCreditRule, uniqueStudentCourses,
				0, 0, false);
		String json = getJSONStringFromObject(minCreditRuleData);

		logger.info("**** Running Rule Engine Min Credits Rule");

		MinCreditRuleData result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_RUN_MIN_CREDIT_RULES, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MinCreditRuleData.class).getBody();

		return result;
	}

	private MatchRuleData runMatchRules(GradProgramRules matchRules, StudentCourses uniqueStudentCourses, CourseRequirements courseRequirements) {
		MatchRuleData matchRuleData = new MatchRuleData(matchRules, uniqueStudentCourses, courseRequirements);
		String json = getJSONStringFromObject(matchRuleData);

		logger.info("**** Running Rule Engine Match Rules");

		MatchRuleData result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_RUN_MATCH_RULES, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MatchRuleData.class).getBody();

		return result;
	}

	private MinElectiveCreditRuleData hasMinElectiveCredits(GradProgramRule minElectiveCreditRule, StudentCourses uniqueStudentCourses){
		MinElectiveCreditRuleData minElectiveCreditRuleData = new MinElectiveCreditRuleData(minElectiveCreditRule,
				uniqueStudentCourses, 0, 0, false);
		String json = getJSONStringFromObject(minElectiveCreditRuleData);

		logger.info("**** Running Rule Engine Min Elective Credits Rule");

		MinElectiveCreditRuleData result = restTemplate.exchange(
				GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
						+ GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_RUN_MIN_ELECTIVE_CREDITS_RULES, HttpMethod.POST,
				new HttpEntity<>(json, httpHeaders), MinElectiveCreditRuleData.class).getBody();

		return result;
	}

	private String getGradDate(List<StudentCourse> studentCourses, List<StudentAssessment> studentAssessments) {

		Date gradDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		try {
			gradDate = dateFormat.parse("1700/01/01");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		studentCourses = studentCourses
				.stream()
				.filter(sc -> sc.isUsed())
				.collect(Collectors.toList());

		for (StudentCourse studentCourse : studentCourses) {
			try {
				if (dateFormat.parse(studentCourse.getSessionDate() + "/01").compareTo(gradDate) > 0) {
					gradDate = dateFormat.parse(studentCourse.getSessionDate() + "/01");
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		return dateFormat.format(gradDate).toString();
	}

	private String getGPA(List<StudentCourse> studentCourseList, List<StudentAssessment> studentAssessmentList,
						 List<GradLetterGrade> gradLetterGradesList) {

		studentCourseList = studentCourseList.stream().filter(sc -> sc.isUsed()).collect(Collectors.toList());
		float totalCredits = studentCourseList.stream().filter(sc -> sc.isUsed()).mapToInt(sc -> sc.getCreditsUsedForGrad()).sum();
		float acquiredCredits = 0;
		String tempGpaMV = "0";

		for (StudentCourse sc : studentCourseList) {
			tempGpaMV = "0";

			GradLetterGrade letterGrade = gradLetterGradesList
					.stream()
					.filter(lg -> lg.getLetterGrade().compareToIgnoreCase(sc.getInterimLetterGrade()) == 0)
					.findFirst().orElse(null);

			if (letterGrade != null) {
				tempGpaMV = letterGrade.getGpaMarkValue();
			}

			float gpaMarkValue = Float.valueOf(tempGpaMV);

			acquiredCredits += (gpaMarkValue * sc.getCreditsUsedForGrad());

			logger.debug("Letter Grade: " + letterGrade + " | GPA Mark Value: " + gpaMarkValue
					+ " | Acquired Credits: " + acquiredCredits + " | Total Credits: " + totalCredits);
		}

		float finalGPA = acquiredCredits / totalCredits;

		DecimalFormat df = new DecimalFormat("0.00");

		return df.format(finalGPA);
	}

	private boolean getHonoursFlag(String GPA) {

		if (Float.valueOf(GPA) > 3)
			return true;
		else
			return false;
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
