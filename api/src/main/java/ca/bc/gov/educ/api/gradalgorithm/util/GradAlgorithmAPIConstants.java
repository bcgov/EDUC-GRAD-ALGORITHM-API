package ca.bc.gov.educ.api.gradalgorithm.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Component
@Getter
@Setter
public class GradAlgorithmAPIConstants {

    public static final String CORRELATION_ID = "correlationID";
    public static final String USER_NAME = "User-Name";
    public static final String REQUEST_SOURCE = "Request-Source";
    public static final String API_NAME = "EDUC-GRAD-ALGORITHM-API";

    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_ALGORITHM_API_ROOT_MAPPING = "/api/" + API_VERSION + "/grad-algorithm";

    //Attribute Constants

    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "GradAlgorithmAPI";
    protected static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GradAlgorithmAPI";
    protected static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SECONDARY_DATE_FORMAT = "yyyy/MM/dd";

    //Rule Type Constants
    public static final String RULE_TYPE_MATCH = "Match";
    public static final String RULE_TYPE_MIN_CREDITS = "MinCredits";
    public static final String RULE_TYPE_MIN_CREDITS_ELECTIVE = "MinCreditsElective";
    public static final String RULE_TYPE_ACTIVE_FLAG_Y = "Y";
    public static final String RULE_TYPE_ACTIVE_FLAG_N = "N";

    
    @Value("${endpoint.rule-engine-api.run-grad-algorithm-rules.url}")
    private String runRules;
    
    @Value("${endpoint.grad-trax-api.school-all-school.url}")
    private String allSchools;
    
    @Value("${endpoint.grad-program-api.get-program-algorithm-data.url}")
    private String programData;
    
    @Value("${endpoint.grad-program-api.get-optional-program.url}")
    private String optionalProgram;
    
    @Value("${endpoint.grad-course-api.get-course-algorithm-data.url}")
    private String courseData;
    
    @Value("${endpoint.grad-assessment-api.get-assessment-algorithm-data.url}")
    private String assessmentData;
    
    @Value("${endpoint.grad-student-graduation-api.get-graduation-message.url}")
    private String graduationMessage;
    
    @Value("${endpoint.grad-student-graduation-api.get-algorithm-data.url}")
    private String studentGraduationAlgorithmURL;
    
    @Value("${endpoint.grad-student-api.get-student-by-studentid.url}")
    private String studentDemographics;
    
    @Value("${endpoint.grad-student-api.get-graduation-student-record.get-optional-programs.url}")
    private String studentOptionalPrograms;
    
    @Value("${endpoint.grad-student-api.get-graduation-status.url}")
    private String graduationStudentRecord;
    
    @Value("${endpoint.grad-student-api.get-grad-student-algo-data.url}")
    private String gradStudentAlgorithmData;

    // Splunk LogHelper Enabled
    @Value("${splunk.log-helper.enabled}")
    private boolean splunkLogHelperEnabled;

    @Value("${authorization.user}")
    private String userName;

    @Value("${authorization.password}")
    private String password;

    @Value("${endpoint.keycloak.getToken}")
    private String tokenUrl;


//Application Properties Constants

}
