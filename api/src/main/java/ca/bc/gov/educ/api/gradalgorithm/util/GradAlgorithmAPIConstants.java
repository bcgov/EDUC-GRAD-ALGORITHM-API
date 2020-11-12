package ca.bc.gov.educ.api.gradalgorithm.util;

import java.util.Date;

public class GradAlgorithmAPIConstants {
    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_ALGORITHM_API_ROOT_MAPPING = "/api/" + API_VERSION + "/grad-algorithm";

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
    public static final String ENDPOINT_GRADSTUDENT_BY_PEN_URL = "${endpoint.grad-student-api.get-student-by-pen.url}";

}
