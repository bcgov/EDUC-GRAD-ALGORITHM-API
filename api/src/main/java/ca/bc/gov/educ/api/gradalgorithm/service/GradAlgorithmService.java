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

import ca.bc.gov.educ.api.gradalgorithm.struct.*;

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

    boolean isGraduated = true;
    HttpHeaders httpHeaders;

    public GraduationData graduateStudent(String pen, String gradProgram, boolean projected, String accessToken) {
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
        List<StudentAssessment> sAssessments = Arrays.asList(getAllAssessmentsForAStudent(pen));
        ruleProcessorData.setStudentAssessments(sAssessments);
        //Get All course Requirements
        CourseRequirements cReq = getAllCourseRequirements(studentCourses);
        ruleProcessorData.setCourseRequirements(cReq != null ?cReq.getCourseRequirementList():null);        
        //Get All Assessment Requirements
        AssessmentRequirements aReq = getAllAssessmentRequirements(sAssessments);
        ruleProcessorData.setAssessmentRequirements(aReq != null ? aReq.getAssessmentRequirementList():null);
        //Get All Grad Letter Grades
        GradLetterGrades lGrades = getAllLetterGrades();
        ruleProcessorData.setGradLetterGradeList(lGrades != null ? lGrades.getGradLetterGradeList():null);
        //Get All Grad Special Cases
        ruleProcessorData.setGradSpecialCaseList(getAllSpecialCases());
        //Get Grad Algorithm Rules from the DB
        List<GradAlgorithmRules> gradAlgorithmRules = getGradAlgorithmRules(gradProgram);
        ruleProcessorData.setGradAlgorithmRules(gradAlgorithmRules);
        //Get All course restrictions
        CourseRestrictions cRes = getAllCourseRestrictions(studentCourses);
        ruleProcessorData.setCourseRestrictions(cRes != null ? cRes.getCourseRestrictions():null); 
        //Get all Grad Program Rules
        List<GradProgramRule> programRulesList = getProgramRules(gradProgram);
        ruleProcessorData.setGradProgramRules(programRulesList);

        //Set Projected flag
        ruleProcessorData.setProjected(projected);

        //Set Special Program Flag
        ruleProcessorData = checkForSpecialProgram(ruleProcessorData.getGradStudent().getStudentID(), ruleProcessorData);
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
        if (ruleProcessorData.isHasSpecialProgramDualDogwood())
        	ruleProcessorData.setGradSpecialProgramRulesDualDogwood(getSpecialProgramRules(gradProgram, "DD"));

        //Calling Rule Processor
        ruleProcessorData = processGradAlgorithmRules(ruleProcessorData);
        isGraduated = ruleProcessorData.isGraduated();
        //Populate Grad Status Details
        GradAlgorithmGraduationStatus gradStatus = getStudentGraduationStatus(ruleProcessorData.getGradStudent().getStudentID(),pen);
       
        if(isGraduated) {
			if (!gradProgram.equalsIgnoreCase("SCCP")) {
				gradStatus.setProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
			             ruleProcessorData.getStudentAssessments()));
				gradStatus.setGpa(getGPA(ruleProcessorData.getStudentCourses(), ruleProcessorData.getStudentAssessments(),
				        ruleProcessorData.getGradLetterGradeList()));
				gradStatus.setHonoursStanding(getHonoursFlag(gradStatus.getGpa()));
			}			
			if(gradStatus.getSchoolAtGrad() == null) {
				gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
	        }  
        }

        ruleProcessorData.setGradStatus(gradStatus);
        
        //Populating Special Grad Status
        List<SpecialGradAlgorithmGraduationStatus> specialProgramStatusList = new ArrayList<>();
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
        if (ruleProcessorData.isHasSpecialProgramDualDogwood())
        	specialProgramStatusList = getListOfSpecialProgramStatus(pen,gradProgram,"DD",specialProgramStatusList);
		
        ruleProcessorData.setSchool(getSchool(ruleProcessorData.getGradStudent().getSchoolOfRecord()));
        //Convert ruleProcessorData into GraduationData object
		graduationData.setGradStudent(ruleProcessorData.getGradStudent());
		graduationData.setGradStatus(ruleProcessorData.getGradStatus());
		graduationData.setSpecialGradStatus(specialProgramStatusList);
		graduationData.setSchool(ruleProcessorData.getSchool());
		graduationData.setStudentCourses(new StudentCourses(ruleProcessorData.getStudentCourses()));
        graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
        graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
        if(ruleProcessorData.getNonGradReasons() != null)
        	Collections.sort(ruleProcessorData.getNonGradReasons(), Comparator.comparing(GradRequirement::getRule));        
        graduationData.setNonGradReasons(ruleProcessorData.getNonGradReasons());
        
        if(ruleProcessorData.getRequirementsMet() != null)
        	Collections.sort(ruleProcessorData.getRequirementsMet(), Comparator.comparing(GradRequirement::getRule));        
        graduationData.setRequirementsMet(ruleProcessorData.getRequirementsMet());
        graduationData.setGraduated(ruleProcessorData.isGraduated());
        
        if(graduationData.isGraduated()) {
        	graduationData.setGradMessage(getGradMessages(gradProgram,"GRADUATED",graduationData.getGradStatus().getProgramCompletionDate(),graduationData.getGradStatus().getHonoursStanding()));
        }else {
        	graduationData.setGradMessage(getGradMessages(gradProgram,"NOT_GRADUATED",graduationData.getGradStatus().getProgramCompletionDate(),graduationData.getGradStatus().getHonoursStanding()));
        }

        logger.info("\n************* Graduation Algorithm END  ************");

        return graduationData;
    }

	/******************************************************************************************************************
	Utility Methods
	*******************************************************************************************************************/
    
    private List<SpecialGradAlgorithmGraduationStatus> getListOfSpecialProgramStatus(String pen,String gradProgram, String specialProgramCode,List<SpecialGradAlgorithmGraduationStatus> specialProgramStatusList) {
    	List<GradRequirement> nonGradReasons = new ArrayList<>();
    	List<GradRequirement> reqMet = new ArrayList<>();
    	SpecialGradAlgorithmGraduationStatus gradStudentSpecialAlg = new SpecialGradAlgorithmGraduationStatus();
		gradStudentSpecialAlg.setPen(pen);
		gradStudentSpecialAlg.setSpecialProgramID(getSpecialProgramID(gradProgram,specialProgramCode));
		gradStudentSpecialAlg.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
		
		switch(specialProgramCode) {
		case "FI":
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramFrenchImmersionGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForFrenchImmersion()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsFrenchImmersion();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsFrenchImmersion();
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "CP":
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramCareerProgramGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForCareerProgram()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsCareerProgram();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsCareerProgram();
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "DD":
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramDualDogwoodGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForDualDogwood()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessmentsForDualDogwood()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsDualDogwood();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsDualDogwood();
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "AD":
		case "BC":
		case "BD":
			gradStudentSpecialAlg.setSpecialGraduated(true);
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
						ruleProcessorData.getStudentAssessments()));
			}
			break;
		default:
			break;
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

    private RuleProcessorData checkForSpecialProgram(String studentID, RuleProcessorData ruleProcessorData) {
        List<GradStudentSpecialProgram> gradSpecialResponseList = restTemplate.exchange(String.format("https://educ-grad-graduation-status-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/gradstatus/specialprogram/studentid/%s", studentID), HttpMethod.GET,
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
            if (sp.getSpecialProgramCode().equalsIgnoreCase("DD")) {
                ruleProcessorData.setHasSpecialProgramDualDogwood(true);
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
    
    protected GradAlgorithmGraduationStatus getStudentGraduationStatus(String studentID,String pen) {
        logger.debug("GET Grad Student Graduation Status: " + GradAlgorithmAPIConstants.GET_GRADSTATUS_BY_STUDENT_ID_URL + "/*****" + pen.substring(5));
        return restTemplate.exchange(
                String.format(GradAlgorithmAPIConstants.GET_GRADSTATUS_BY_STUDENT_ID_URL,studentID), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), GradAlgorithmGraduationStatus.class).getBody();
    }

    protected GradSearchStudent getStudentDemographics(String pen) {
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

    private StudentAssessment[] getAllAssessmentsForAStudent(String pen) {

        ResponseEntity<StudentAssessment[]> response = restTemplate.exchange(
                "https://student-assessment-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/studentassessment/pen"
                        + "/" + pen, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), StudentAssessment[].class);

        StudentAssessment[] result = new StudentAssessment[0];

        if (response.getStatusCode().value() != 204)
            result = response.getBody();

        logger.info("**** # of Assessments: " + (result != null ? result.length : 0));

        for (StudentAssessment studentAssessment : result) {
        	studentAssessment.setGradReqMet("");
        	studentAssessment.setGradReqMetDetail("");
        }

        return result;
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
        if(result != null)
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
    
    private CourseRestrictions getAllCourseRestrictions(List<StudentCourse> studentCourseList) {
        List<String> courseList = studentCourseList.stream()
                .map(StudentCourse::getCourseCode)
                .distinct()
                .collect(Collectors.toList());
        String json = getJSONStringFromObject(new CourseList(courseList));
        CourseRestrictions result = restTemplate.exchange(
                "https://grad-course-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/course/course-restriction/course-list", HttpMethod.POST,
                new HttpEntity<>(json, httpHeaders), CourseRestrictions.class).getBody();
        logger.info("**** # of Course Restrictions: " + (result != null ? result.getCourseRestrictions().size() : 0));

        return result;
    }
    
    

    private AssessmentRequirements getAllAssessmentRequirements(List<StudentAssessment> studentAssessmentList) {
        List<String> assessmentList = studentAssessmentList.stream()
                .map(StudentAssessment::getAssessmentCode)
                .distinct()
                .collect(Collectors.toList());
        String json = getJSONStringFromObject(new AssessmentList(assessmentList));
        AssessmentRequirements result = restTemplate.exchange(
                "https://grad-assessment-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/assessment/requirement/assessment-list", HttpMethod.POST,
                new HttpEntity<>(json, httpHeaders), AssessmentRequirements.class).getBody();
        logger.info("**** # of Assessment Requirements: " + (result != null ? result.getAssessmentRequirementList().size() : 0));

        return result;
    }
    
    private GradLetterGrades getAllLetterGrades() {
        GradLetterGrades result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/lettergrade", HttpMethod.GET,
                new HttpEntity<>(httpHeaders), GradLetterGrades.class).getBody();
        logger.info("**** # of Letter Grades: " + (result != null ? result.getGradLetterGradeList().size() : 0));

        return result;
    }
    
    private List<GradSpecialCase> getAllSpecialCases() {
        List<GradSpecialCase> result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/specialcase", HttpMethod.GET,
                new HttpEntity<>(httpHeaders),new ParameterizedTypeReference<List<GradSpecialCase>>() {
                }).getBody();
        logger.info("**** # of Special Cases: " + (result != null ? result.size() : 0));

        return result;
    }
    
    private String getGradMessages(String gradProgram, String msgType,String gradDate,String honours) {
		StringBuilder strBuilder = new StringBuilder();
		GradMessaging result = restTemplate.exchange(
                String.format("https://educ-grad-code-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/code/gradmessages/pgmCode/%s/msgType/%s",gradProgram,msgType), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), GradMessaging.class).getBody();
		if(result != null) {
			if(isGraduated) {
				if(!gradProgram.equalsIgnoreCase("SCCP")) {
					if(honours.equalsIgnoreCase("Y")) {
						strBuilder.append(String.format(result.getHonours(),gradProgram));
					}else {
						strBuilder.append(String.format(result.getMainMessage(),gradProgram));
					}
					strBuilder.append(System.getProperty("line.separator")).append(String.format(result.getGradDate(),gradDate));
				}else {
					strBuilder.append(String.format(result.getMainMessage(),gradProgram));
				}
			}else {
				strBuilder.append(String.format(result.getMainMessage(),gradProgram));
			}		
	        return strBuilder.toString();
		}
		return null;
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
            e.getMessage();
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
                e.getMessage();
            }
        }
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(gradDate);
    }

    private String getGPA(List<StudentCourse> studentCourseList, List<StudentAssessment> studentAssessmentList,
                          List<GradLetterGrade> gradLetterGradesList) {

        studentCourseList = studentCourseList.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
        float totalCredits = studentCourseList.stream().filter(StudentCourse::isUsed).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
        float acquiredCredits = 0;
        String tempGpaMV =null;

        for (StudentCourse sc : studentCourseList) {
            tempGpaMV = "0";
            String completedCourseGrade = sc.getCompletedCourseLetterGrade() != null ? sc.getCompletedCourseLetterGrade():"";
            GradLetterGrade letterGrade = gradLetterGradesList
                    .stream()
                    .filter(lg -> lg.getLetterGrade().compareToIgnoreCase(completedCourseGrade) == 0)
                    .findFirst().orElse(null);

            if (letterGrade != null) {
                tempGpaMV = letterGrade.getGpaMarkValue();
            }else {
            	if(sc.getCompletedCourseLetterGrade().equalsIgnoreCase("RM") 
            			|| sc.getCompletedCourseLetterGrade().equalsIgnoreCase("SG") 
            			|| sc.getCompletedCourseLetterGrade().equalsIgnoreCase("TS")) {
            		tempGpaMV = "0";		
            	}
            }

            float gpaMarkValue = Float.parseFloat(tempGpaMV);

            acquiredCredits += (gpaMarkValue * sc.getCreditsUsedForGrad());

            logger.debug("Letter Grade: " + letterGrade + " | GPA Mark Value: " + gpaMarkValue
                    + " | Acquired Credits: " + acquiredCredits + " | Total Credits: " + totalCredits);
        }

        float finalGPA = acquiredCredits / totalCredits;

        DecimalFormat df = new DecimalFormat("0.00");
        if(Float.isNaN(finalGPA)) {
        	return "0.00";
        }
        return df.format(finalGPA);
    }

    private String getHonoursFlag(String gPA) {

        if (Float.parseFloat(gPA) > 3)
            return "Y";
        else
            return "N";
    }

    private School getSchool(String minCode) {
        return restTemplate.exchange("https://educ-grad-school-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/school" + "/" + minCode, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), School.class).getBody();
    }

    private <T> String getJSONStringFromObject(T inputObject) {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        try {
            json = mapper.writeValueAsString(inputObject);
        } catch (JsonProcessingException e) {
            e.getMessage();
        }

        return json;
    }

    private UUID getSpecialProgramID(String gradProgram, String gradSpecialProgram) {
        GradSpecialProgram result = restTemplate.exchange(
                "https://educ-grad-program-management-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/programmanagement/specialprograms/" + gradProgram + "/" + gradSpecialProgram, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), GradSpecialProgram.class).getBody();
        return result != null ? result.getId() : null;
    }
}