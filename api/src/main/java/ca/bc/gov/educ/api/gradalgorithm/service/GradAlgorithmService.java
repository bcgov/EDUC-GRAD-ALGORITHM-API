package ca.bc.gov.educ.api.gradalgorithm.service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradalgorithm.struct.CourseList;
import ca.bc.gov.educ.api.gradalgorithm.struct.CourseRequirements;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradAlgorithmRules;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradLetterGrade;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradLetterGrades;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradProgramRule;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradRequirement;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradSpecialProgramRule;
import ca.bc.gov.educ.api.gradalgorithm.struct.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.struct.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.struct.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.struct.School;
import ca.bc.gov.educ.api.gradalgorithm.struct.SpecialGradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentAssessments;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentCourses;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentExams;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradAlgorithmService {

    private static final Logger logger = LoggerFactory.getLogger(GradAlgorithmService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    RuleProcessorData ruleProcessorData;

    @Autowired
    GraduationData graduationData;

    @Autowired
    StudentAssessments studentAssessments;

    @Autowired
    StudentExams studentExams;

    @Autowired
    CourseRequirements courseRequirements;

    boolean isGraduated = true;
    boolean isGraduatedSpecialProgram = true;

    HttpHeaders httpHeaders;

    public GraduationData graduateStudentNew(String pen, String gradProgram, boolean projected, String accessToken) {
        logger.info("\n************* New Graduation Algorithm START  ************");
        httpHeaders = APIUtils.getHeaders(accessToken);
        logger.info("**** PEN: ****" + pen.substring(5));
        logger.info("**** Grad Program: " + gradProgram);
        ruleProcessorData = new RuleProcessorData();
        //Get Student Demographics
        ruleProcessorData.setGradStudent(getStudentDemographics(pen));
        //Get All Courses for a Student
        List<StudentCourse> studentCourses = Arrays.asList(getAllCoursesForAStudent(pen));
        ruleProcessorData.setStudentCourses(studentCourses);
        //Get All Assessments for a Student
        ruleProcessorData.setStudentAssessments(getAllAssessmentsForAStudent(pen).getStudentAssessmentList());
        //Get All course Requirements
        ruleProcessorData.setCourseRequirements(getAllCourseRequirements(studentCourses).getCourseRequirementList());
        //Get All Grad Letter Grades
        ruleProcessorData.setGradLetterGradeList(getAllLetterGrades().getGradLetterGradeList());

        //Get Grad Algorithm Rules from the DB
        List<GradAlgorithmRules> gradAlgorithmRules = getGradAlgorithmRules(gradProgram);
        ruleProcessorData.setGradAlgorithmRules(gradAlgorithmRules);

        //Get all Grad Program Rules
        List<GradProgramRule> programRulesList = getProgramRules(gradProgram);
        ruleProcessorData.setGradProgramRules(programRulesList);

        //Set Projected flag
        ruleProcessorData.setProjected(projected);

        //Set Special Program Flag
        ruleProcessorData = checkForSpecialProgram(pen, ruleProcessorData);
        if (ruleProcessorData.isHasSpecialProgramFrenchImmersion())
            ruleProcessorData.setGradSpecialProgramRulesFrenchImmersion(getSpecialProgramRules(gradProgram, "FI"));
        if (ruleProcessorData.isHasSpecialProgramAdvancedPlacement())
            ruleProcessorData.setGradSpecialProgramRulesAdvancedPlacement(getSpecialProgramRules(gradProgram, "AD"));
        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBD())
            ruleProcessorData.setGradSpecialProgramRulesInternationalBaccalaureateBD(getSpecialProgramRules(gradProgram, "BD"));
        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBC())
            ruleProcessorData.setGradSpecialProgramRulesInternationalBaccalaureateBC(getSpecialProgramRules(gradProgram, "BC"));
        if (ruleProcessorData.isHasSpecialProgramCareerProgram())
            ruleProcessorData.setGradSpecialProgramRulesCareerProgram(getSpecialProgramRules(gradProgram, "CP"));

        //Calling Rule Processor
        ruleProcessorData = processGradAlgorithmRules(ruleProcessorData);

        //Populate Grad Status Details
        GradAlgorithmGraduationStatus gradStatus = new GradAlgorithmGraduationStatus();
        gradStatus.setPen(pen);
        gradStatus.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
        gradStatus.setProgram(gradProgram);
        if (isGraduated) {
            gradStatus.setProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
                    ruleProcessorData.getStudentAssessments()));
        }
        gradStatus.setGpa(getGPA(ruleProcessorData.getStudentCourses(), ruleProcessorData.getStudentAssessments(),
                ruleProcessorData.getGradLetterGradeList()));
        gradStatus.setHonoursStanding(getHonoursFlag(gradStatus.getGpa()));
        gradStatus.setSchoolOfRecord(ruleProcessorData.getGradStudent().getSchoolOfRecord());

        ruleProcessorData.setGradStatus(gradStatus);
        
        //Populating Special Grad Status
        List<SpecialGradAlgorithmGraduationStatus> specialProgramStatusList = new ArrayList<SpecialGradAlgorithmGraduationStatus>();
        if (ruleProcessorData.isHasSpecialProgramFrenchImmersion())
        	specialProgramStatusList = getListOfSpecialProgramStatus(pen,gradProgram,"FI",specialProgramStatusList);
        if (ruleProcessorData.isHasSpecialProgramCareerProgram())
        	specialProgramStatusList = getListOfSpecialProgramStatus(pen,gradProgram,"CP",specialProgramStatusList);
        if (ruleProcessorData.isHasSpecialProgramAdvancedPlacement())
        	specialProgramStatusList = getListOfSpecialProgramStatus(pen,gradProgram,"AD",specialProgramStatusList);
        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBD())
        	specialProgramStatusList = getListOfSpecialProgramStatus(pen,gradProgram,"BD",specialProgramStatusList);
        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBC())
        	specialProgramStatusList = getListOfSpecialProgramStatus(pen,gradProgram,"BC",specialProgramStatusList);
		
        
        ruleProcessorData.setSchool(getSchool(ruleProcessorData.getGradStudent().getSchoolOfRecord()));

        //Convert ruleProcessorData into GraduationData object
		graduationData.setGradStudent(ruleProcessorData.getGradStudent());
		graduationData.setGradStatus(ruleProcessorData.getGradStatus());
		graduationData.setSpecialGradStatus(specialProgramStatusList);
		graduationData.setSchool(ruleProcessorData.getSchool());
		graduationData.setStudentCourses(new StudentCourses(ruleProcessorData.getStudentCourses()));
        graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
        graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
        graduationData.setNonGradReasons(ruleProcessorData.getNonGradReasons());
        graduationData.setRequirementsMet(ruleProcessorData.getRequirementsMet());
        graduationData.setGraduated(ruleProcessorData.isGraduated());

        logger.info("\n************* New Graduation Algorithm END  ************");

        return graduationData;
    }

	/*
	********************************************************************************************************************
	Utility Methods
	********************************************************************************************************************
	 */
    
    private List<SpecialGradAlgorithmGraduationStatus> getListOfSpecialProgramStatus(String pen,String gradProgram, String specialProgramCode,List<SpecialGradAlgorithmGraduationStatus> specialProgramStatusList) {
    	List<GradRequirement> nonGradReasons = new ArrayList<GradRequirement>();
    	List<GradRequirement> reqMet = new ArrayList<GradRequirement>();
    	SpecialGradAlgorithmGraduationStatus gradStudentSpecialAlg = new SpecialGradAlgorithmGraduationStatus();
		gradStudentSpecialAlg.setPen(pen);
		gradStudentSpecialAlg.setSpecialProgramID(getSpecialProgramID(gradProgram,specialProgramCode));
		gradStudentSpecialAlg.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
		
		if("FI".equalsIgnoreCase(specialProgramCode)) {
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramFrenchImmersionGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForFrenchImmersion()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsFrenchImmersion();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsFrenchImmersion();
			if (gradStudentSpecialAlg.isSpecialGraduated()) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			
		}
		if("CP".equalsIgnoreCase(specialProgramCode)) {
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramCareerProgramGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForCareerProgram()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsCareerProgram();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsCareerProgram();
			if (gradStudentSpecialAlg.isSpecialGraduated()) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
		}
		if("AD".equalsIgnoreCase(specialProgramCode)) {
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramAdvancedPlacementGraduated());
			if (gradStudentSpecialAlg.isSpecialGraduated()) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
						ruleProcessorData.getStudentAssessments()));
			}
		}
		if("BC".equalsIgnoreCase(specialProgramCode)) {
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramInternationalBaccalaureateGraduatedBC());
			if (gradStudentSpecialAlg.isSpecialGraduated()) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
						ruleProcessorData.getStudentAssessments()));
			}
		}
		if("BD".equalsIgnoreCase(specialProgramCode)) {
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramInternationalBaccalaureateGraduatedBD());
			if (gradStudentSpecialAlg.isSpecialGraduated()) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
						ruleProcessorData.getStudentAssessments()));
			}
		}
		
		if(nonGradReasons != null)
			Collections.sort(nonGradReasons, Comparator.comparing(GradRequirement::getRule));
		
		gradStudentSpecialAlg.setSpecialNonGradReasons(nonGradReasons);
		if(reqMet != null)
			Collections.sort(reqMet,Comparator.comparing(GradRequirement::getRule));
		
		gradStudentSpecialAlg.setSpecialRequirementsMet(reqMet);
		
		specialProgramStatusList.add(gradStudentSpecialAlg);
		return specialProgramStatusList;
    }

    private RuleProcessorData checkForSpecialProgram(String pen, RuleProcessorData ruleProcessorData) {
        List<GradStudentSpecialProgram> gradSpecialResponseList = restTemplate.exchange(String.format("https://educ-grad-graduation-status-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/gradstatus/specialprogram/pen/%s", pen), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradStudentSpecialProgram>>() {
                }).getBody();

        if (gradSpecialResponseList == null)
            return ruleProcessorData;

        for (GradStudentSpecialProgram sp : gradSpecialResponseList) {
            if (sp.getSpecialProgramCode().equalsIgnoreCase("FI")) {
                ruleProcessorData.setHasSpecialProgramFrenchImmersion(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("AD")) {
                ruleProcessorData.setHasSpecialProgramAdvancedPlacement(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("BD")) {
                ruleProcessorData.setHasSpecialProgramInternationalBaccalaureateBD(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("BC")) {
                ruleProcessorData.setHasSpecialProgramInternationalBaccalaureateBC(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("CP")) {
                ruleProcessorData.setHasSpecialProgramCareerProgram(true);
            }
        }
        return ruleProcessorData;
    }

    private List<GradAlgorithmRules> getGradAlgorithmRules(String gradProgram) {
        List<GradAlgorithmRules> result = restTemplate.exchange(
                "https://educ-grad-common-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/common/algorithm-rules/main/" + gradProgram, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradAlgorithmRules>>() {
                }).getBody();
        logger.info("**** # of Grad Algorithm Rules: " + (result != null ? result.size() : 0));

        return result;
    }

    private GradSearchStudent getStudentDemographics(String pen) {
        logger.debug("GET Grad Student Demographics: " + GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/*****" + pen.substring(5));
        List<GradSearchStudent> resultList = restTemplate.exchange(
                GradAlgorithmAPIConstants.GET_GRADSTUDENT_BY_PEN_URL + "/" + pen, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradSearchStudent>>() {
                }).getBody();
        GradSearchStudent result = resultList.get(0);
        logger.debug((result != null ? result.getLegalLastName().trim() : null) + ", "
                + (result != null ? result.getLegalFirstName().trim() : null));

        return result;
    }

    private StudentCourse[] getAllCoursesForAStudent(String pen) {
        ResponseEntity<StudentCourse[]> response = restTemplate.exchange(
                GradAlgorithmAPIConstants.GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), StudentCourse[].class);

        StudentCourse[] result = new StudentCourse[0];

        if (response.getStatusCode().value() != 204)
            result = response.getBody();

        logger.info("**** # of courses: " + (result != null ? result.length : 0));

        for (StudentCourse studentCourse : result) {
            studentCourse.setGradReqMet("");
            studentCourse.setGradReqMetDetail("");
        }

        return result;
    }

    private StudentAssessments getAllAssessmentsForAStudent(String pen) {

        ResponseEntity<StudentAssessment[]> response = restTemplate.exchange(
                "https://student-assessment-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/studentassessment/pen"
                        + "/" + pen, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), StudentAssessment[].class);

        StudentAssessment[] result = new StudentAssessment[0];

        if (response.getStatusCode().value() != 204)
            result = response.getBody();

        logger.info("**** # of Assessments: " + (result != null ? result.length : 0));

        this.studentAssessments.setStudentAssessmentList(
                Arrays.asList(result != null ? result.clone() : new StudentAssessment[0]));

        return studentAssessments;
    }

    private List<GradProgramRule> getProgramRules(String programCode) {
        List<GradProgramRule> result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/" +
                        "programrules?programCode=" + programCode, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradProgramRule>>() {
                }).getBody();
        logger.info("**** # of Program Rules: " + (result != null ? result.size() : 0));

        return result;
    }

    private List<GradSpecialProgramRule> getSpecialProgramRules(String gradProgram, String gradSpecialProgram) {
        List<GradSpecialProgramRule> result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/" +
                        "specialprogramrules/" + gradProgram + "/" + gradSpecialProgram, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<GradSpecialProgramRule>>() {
                }).getBody();
        logger.info("**** # of Special Program Rules: " + result.size());
        return result;
    }

    private CourseRequirements getAllCourseRequirements(List<StudentCourse> studentCourseList) {
        List<String> courseList = studentCourseList.stream()
                .map(StudentCourse::getCourseCode)
                .distinct()
                .collect(Collectors.toList());
        String json = getJSONStringFromObject(new CourseList(courseList));
        CourseRequirements result = restTemplate.exchange(
                "https://grad-course-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/course/course-requirement/course-list", HttpMethod.POST,
                new HttpEntity<>(json, httpHeaders), CourseRequirements.class).getBody();
        logger.info("**** # of Course Requirements: " + (result != null ? result.getCourseRequirementList().size() : 0));

        return result;
    }

    private GradLetterGrades getAllLetterGrades() {
        GradLetterGrades result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/lettergrade", HttpMethod.GET,
                new HttpEntity<>(httpHeaders), GradLetterGrades.class).getBody();
        logger.info("**** # of Letter Grades: " + (result != null ? result.getGradLetterGradeList().size() : 0));

        return result;
    }

    private RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData) {

        String json = getJSONStringFromObject(ruleProcessorData);

        logger.info("**** Processing Grad Algorithm Rules");

        return restTemplate.exchange(
                GradAlgorithmAPIConstants.RULE_ENGINE_API_BASE_URL + "/"
                        + GradAlgorithmAPIConstants.RULE_ENGINE_API_ENDPOINT_RUN_GRAD_ALGORITHM_RULES, HttpMethod.POST,
                new HttpEntity<>(json, httpHeaders), RuleProcessorData.class).getBody();
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
                .filter(StudentCourse::isUsed)
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

        studentCourseList = studentCourseList.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
        float totalCredits = studentCourseList.stream().filter(StudentCourse::isUsed).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
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

            float gpaMarkValue = Float.parseFloat(tempGpaMV);

            acquiredCredits += (gpaMarkValue * sc.getCreditsUsedForGrad());

            logger.debug("Letter Grade: " + letterGrade + " | GPA Mark Value: " + gpaMarkValue
                    + " | Acquired Credits: " + acquiredCredits + " | Total Credits: " + totalCredits);
        }

        float finalGPA = acquiredCredits / totalCredits;

        DecimalFormat df = new DecimalFormat("0.00");

        return df.format(finalGPA);
    }

    private String getHonoursFlag(String GPA) {

        if (Float.parseFloat(GPA) > 3)
            return "Y";
        else
            return "N";
    }

    private School getSchool(String minCode) {

        School schObj = restTemplate.exchange("https://educ-grad-school-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/school" + "/" + minCode, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), School.class).getBody();
        return schObj;
    }

    private <T> String getJSONStringFromObject(T inputObject) {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        try {
            json = mapper.writeValueAsString(inputObject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    private UUID getSpecialProgramID(String gradProgram, String gradSpecialProgram) {
        GradSpecialProgram result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/specialprograms/" + gradProgram + "/" + gradSpecialProgram, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), GradSpecialProgram.class).getBody();
        return result.getId();
    }
}
