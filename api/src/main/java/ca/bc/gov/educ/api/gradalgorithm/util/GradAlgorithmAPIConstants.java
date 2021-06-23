package ca.bc.gov.educ.api.gradalgorithm.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class GradAlgorithmAPIConstants {
    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_ALGORITHM_API_ROOT_MAPPING = "/api/" + API_VERSION + "/grad-algorithm";

    public static String GET_GRADSTUDENT_BY_PEN_URL;
    public static String GET_STUDENT_COURSES_BY_PEN_URL;
    public static String RULE_ENGINE_API_BASE_URL;
    public static String RULE_ENGINE_API_ENDPOINT_FIND_NOT_COMPLETED;
    public static String RULE_ENGINE_API_ENDPOINT_FIND_PROJECTED;
    public static String RULE_ENGINE_API_ENDPOINT_FIND_FAILED;
    public static String RULE_ENGINE_API_ENDPOINT_FIND_DUPLICATES;
    public static String RULE_ENGINE_API_ENDPOINT_FIND_CP;
    public static String RULE_ENGINE_API_ENDPOINT_FIND_LD;
    public static String RULE_ENGINE_API_ENDPOINT_RUN_MIN_CREDIT_RULES;
    public static String RULE_ENGINE_API_ENDPOINT_RUN_MATCH_RULES;
    public static String RULE_ENGINE_API_ENDPOINT_RUN_MIN_ELECTIVE_CREDITS_RULES;
    public static String RULE_ENGINE_API_ENDPOINT_RUN_SPECIAL_MIN_ELECTIVE_CREDITS_RULES;
    public static String RULE_ENGINE_API_ENDPOINT_RUN_SPECIAL_MATCH_RULES;
    public static String RULE_ENGINE_API_ENDPOINT_RUN_GRAD_ALGORITHM_RULES;
    public static String GET_GRADSTATUS_BY_STUDENT_ID_URL;
    public static String GET_STUDENT_ASSESSMENT_BY_PEN;
    public static String GET_SCHOOL_BY_MINCODE;


    //Attribute Constants


    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "GradAlgorithmAPI";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GradAlgorithmAPI";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";

    //Rule Type Constants
    public static final String RULE_TYPE_MATCH = "Match";
    public static final String RULE_TYPE_MIN_CREDITS = "MinCredits";
    public static final String RULE_TYPE_MIN_CREDITS_ELECTIVE = "MinCreditsElective";
    public static final String RULE_TYPE_ACTIVE_FLAG_Y = "Y";
    public static final String RULE_TYPE_ACTIVE_FLAG_N = "N";

    @Value("${endpoint.grad-student-api.get-student-by-pen.url}")
    public void setGetGradstudentByPenUrl(String getGradstudentByPenUrl) {
        GET_GRADSTUDENT_BY_PEN_URL = getGradstudentByPenUrl;
    }

    @Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
    public void setGetStudentCoursesByPenUrl(String getStudentCoursesByPenUrl) {
        GET_STUDENT_COURSES_BY_PEN_URL = getStudentCoursesByPenUrl;
    }

    @Value("${endpoint.rule-engine-api.base-url}")
    public void setRuleEngineApiBaseUrl(String ruleEngineApiBaseUrl) {
        RULE_ENGINE_API_BASE_URL = ruleEngineApiBaseUrl;
    }

    @Value("${endpoint.rule-engine-api.endpoints.find-not-completed}")
    public void setRuleEngineApiEndpointFindNotCompleted(String ruleEngineApiEndpointFindNotCompleted) {
        RULE_ENGINE_API_ENDPOINT_FIND_NOT_COMPLETED = ruleEngineApiEndpointFindNotCompleted;
    }

    @Value("${endpoint.rule-engine-api.endpoints.find-projected}")
    public void setRuleEngineApiEndpointFindProjected(String ruleEngineApiEndpointFindProjected) {
        RULE_ENGINE_API_ENDPOINT_FIND_PROJECTED = ruleEngineApiEndpointFindProjected;
    }

    @Value("${endpoint.rule-engine-api.endpoints.find-failed}")
    public void setRuleEngineApiEndpointFindFailed(String ruleEngineApiEndpointFindFailed) {
        RULE_ENGINE_API_ENDPOINT_FIND_FAILED = ruleEngineApiEndpointFindFailed;
    }

    @Value("${endpoint.rule-engine-api.endpoints.find-duplicates}")
    public void setRuleEngineApiEndpointFindDuplicates(String ruleEngineApiEndpointFindDuplicates) {
        RULE_ENGINE_API_ENDPOINT_FIND_DUPLICATES = ruleEngineApiEndpointFindDuplicates;
    }

    @Value("${endpoint.rule-engine-api.endpoints.find-cp}")
    public void setRuleEngineApiEndpointFindCp(String ruleEngineApiEndpointFindCp) {
        RULE_ENGINE_API_ENDPOINT_FIND_CP = ruleEngineApiEndpointFindCp;
    }

    @Value("${endpoint.rule-engine-api.endpoints.find-ld}")
    public void setRuleEngineApiEndpointFindLd(String ruleEngineApiEndpointFindLd) {
        RULE_ENGINE_API_ENDPOINT_FIND_LD = ruleEngineApiEndpointFindLd;
    }

    @Value("${endpoint.rule-engine-api.endpoints.run-min-credits-rules}")
    public void setRuleEngineApiEndpointRunMinCreditRules(String ruleEngineApiEndpointRunMinCreditRules) {
        RULE_ENGINE_API_ENDPOINT_RUN_MIN_CREDIT_RULES = ruleEngineApiEndpointRunMinCreditRules;
    }

    @Value("${endpoint.rule-engine-api.endpoints.run-match-rules}")
    public void setRuleEngineApiEndpointRunMatchRules(String ruleEngineApiEndpointRunMatchRules) {
        RULE_ENGINE_API_ENDPOINT_RUN_MATCH_RULES = ruleEngineApiEndpointRunMatchRules;
    }

    @Value("${endpoint.rule-engine-api.endpoints.run-min-elective-credits-rules}")
    public void setRuleEngineApiEndpointRunMinElectiveCreditsRules(String ruleEngineApiEndpointRunMinElectiveCreditsRules) {
        RULE_ENGINE_API_ENDPOINT_RUN_MIN_ELECTIVE_CREDITS_RULES = ruleEngineApiEndpointRunMinElectiveCreditsRules;
    }
   
    @Value("${endpoint.rule-engine-api.endpoints.run-special-min-elective-credits-rules}")
    public void setRuleEngineApiEndpointRunSpecialMinElectiveCreditsRules(String ruleEngineApiEndpointRunSpecialMinElectiveCreditsRules) {
        RULE_ENGINE_API_ENDPOINT_RUN_SPECIAL_MIN_ELECTIVE_CREDITS_RULES = ruleEngineApiEndpointRunSpecialMinElectiveCreditsRules;
    }
    
    @Value("${endpoint.rule-engine-api.endpoints.run-special-match-rules}")
    public void setRuleEngineApiEndpointRunSpecialMatchRules(String ruleEngineApiEndpointRunSpecialMatchRules) {
        RULE_ENGINE_API_ENDPOINT_RUN_SPECIAL_MATCH_RULES = ruleEngineApiEndpointRunSpecialMatchRules;
    }

    @Value("${endpoint.rule-engine-api.endpoints.run-grad-algorithm-rules}")
    public void setRuleEngineApiEndpointRunGradAlgorithmRules(String ruleEngineApiEndpointRunGradAlgorithmRules) {
        RULE_ENGINE_API_ENDPOINT_RUN_GRAD_ALGORITHM_RULES = ruleEngineApiEndpointRunGradAlgorithmRules;
    }
    
    @Value("${endpoint.grad-graduation-status-api.get-graduation-status.url}")
    public void setGraduationStatusApiEndpointGetGradStatus(String graduationStatusApiEndpointGetGradStatus) {
    	GET_GRADSTATUS_BY_STUDENT_ID_URL = graduationStatusApiEndpointGetGradStatus;
    }

    @Value("${endpoint.student-assessment-api.get-student-assessment-by-pen.url}")
    public void setGradStudentAssessmentByPenUrl(String gradStudentAssessmentByPenUrl) {
        GET_STUDENT_ASSESSMENT_BY_PEN = gradStudentAssessmentByPenUrl;
    }

    @Value("${endpoint.school-api.school-by-min-code.url}")
    public void setGetSchoolByMincodeUrl(String getSchoolByMincodeUrl) {
        GET_SCHOOL_BY_MINCODE = getSchoolByMincodeUrl;
    }
    

//Application Properties Constants

}
