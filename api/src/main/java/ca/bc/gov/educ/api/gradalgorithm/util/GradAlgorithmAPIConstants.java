package ca.bc.gov.educ.api.gradalgorithm.util;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

public class GradAlgorithmAPIConstants {
    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_ALGORITHM_API_ROOT_MAPPING = "/api/" + API_VERSION + "/grad-algorithm";

    @Value("${endpoint.grad-student-api.get-student-by-pen.url}")
    public static String GET_GRADSTUDENT_BY_PEN_URL;

    @Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
    public static String GET_STUDENT_COURSES_BY_PEN_URL;

    @Value("${endpoint.rule-engine-api.base-url}")
    public static String RULE_ENGINE_API_BASE_URL;

    @Value("${endpoint.rule-engine-api.endpoints.find-not-completed}")
    public static String RULE_ENGINE_API_ENDPOINT_FIND_NOT_COMPLETED;

    @Value("${endpoint.rule-engine-api.endpoints.find-projected}")
    public static String RULE_ENGINE_API_ENDPOINT_FIND_PROJECTED;

    @Value("${endpoint.rule-engine-api.endpoints.find-failed}")
    public static String RULE_ENGINE_API_ENDPOINT_FIND_FAILED;

    @Value("${endpoint.rule-engine-api.endpoints.find-duplicates}")
    public static String RULE_ENGINE_API_ENDPOINT_FIND_DUPLICATES;

    @Value("${endpoint.rule-engine-api.endpoints.run-min-credits-rules}")
    public static String RULE_ENGINE_API_ENDPOINT_RUN_MIN_CREDIT_RULES;

    @Value("${endpoint.rule-engine-api.endpoints.run-match-rules}")
    public static String RULE_ENGINE_API_ENDPOINT_RUN_MATCH_RULES;

    @Value("${endpoint.rule-engine-api.endpoints.run-min-elective-credits-rules}")
    public static String RULE_ENGINE_API_ENDPOINT_RUN_MIN_ELECTIVE_CREDITS_RULES;

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

    //Application Properties Constants

}
