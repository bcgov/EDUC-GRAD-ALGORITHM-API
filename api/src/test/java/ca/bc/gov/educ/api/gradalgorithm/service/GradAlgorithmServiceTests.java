package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAlgorithmServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradAlgorithmServiceTests.class);
    private static final String CLASS_NAME = GradAlgorithmServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradAlgorithmService gradAlgorithmService;

    @MockBean
    WebClient webClient;
    @Autowired
    private GradAlgorithmAPIConstants constants;
    @Autowired
    JsonTransformer jsonTransformer;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;

    @Value("${endpoint.grad-student-api.get-student-by-pen.url}")
    private String getStudentByPenUrl;
    @Value("${endpoint.student-course-api.get-student-course-by-pen.url}")
    private String getStudentCourseByPenUrl;
    @Value("${endpoint.student-assessment-api.get-student-assessment-by-pen.url}")
    private String getStudentAssessmentUrl;
    @Value("${endpoint.course-api.course-requirements-api.url}")
    private String getCourseRequirementsUrl;
    @Value("${endpoint.course-api.course-restriction-api.url}")
    private String getCourseRestrictionUrl;
    @Value("${endpoint.assessment-api.assessment-api-requirement-assessments.url}")
    private String getAssessmentRequirementsUrl;
    @Value("${endpoint.grad-program-management-api.program_management_base.url}")
    private String programManagementBaseUrl;
    @Value("${endpoint.gradalgorithm-api.grad-common-api.algorithm-rules-main-grad-program.url}")
    private String getAlgorithmRulesMainGradProgramUrl;
    @Value("${endpoint.rule-engine-api.base-url}")
    private String ruleEngineBaseUrl;
    @Value("${endpoint.rule-engine-api.endpoints.run-grad-algorithm-rules}")
    private String ruleEngineRunGradAlgorithmRulesUrl;
    @Value("${endpoint.grad-graduation-status-api.grad-status-base.url}")
    private String getGraduaationStatusBaseUrl;
    @Value("${endpoint.grad-graduation-status-api.get-graduation-status.url}")
    private String getGraduationStatusUrl;
    @Value("${endpoint.school-api.school-by-min-code.url}")
    private String getSchoolByMincodeUrl;
    @Value("${endpoint.assessment-api.assessment-api-base.url}")
    private String getAssessmentBaseUrl;
    @Value("${endpoint.code-api.grad-messages.grad-messages-program-code.url}")
    private String getCodeGradMessagesProgramCodeUrl;

    @BeforeClass
    public static void setup() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Before
    public void init() throws Exception {
        openMocks(this);
    }

    @Test
    public void graduateStudentTest() throws Exception {
        LOG.debug("<{}.graduateStudentTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String accessToken = "accessToken";

        RuleProcessorData ruleProcessorData = createRuleProcessorData("json/ruleProcessorData.json");

        /** Start Get Student Demographics **/
        List<GradSearchStudent> gradSearchStudents = new ArrayList();
        GradSearchStudent gradSearchStudent = ruleProcessorData.getGradStudent();
        gradSearchStudents.add(gradSearchStudent);

        String pen = gradSearchStudent.getPen();
        String programCode = gradSearchStudent.getProgram();

        ParameterizedTypeReference<List<GradSearchStudent>> studentResponseType = new ParameterizedTypeReference<List<GradSearchStudent>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentByPenUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(gradSearchStudents));

        /** End Get Student Demographics **/

        /** Start Get All Courses for a Student **/

        List<StudentCourse> studentCourses = ruleProcessorData.getStudentCourses();
        StudentCourse[] studentCourseArray = new StudentCourse[studentCourses.size()];
        studentCourseArray = studentCourses.toArray(studentCourseArray);

        ParameterizedTypeReference<StudentCourse[]> courseResponseType = new ParameterizedTypeReference<StudentCourse[]>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentCourseByPenUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(courseResponseType)).thenReturn(Mono.just(studentCourseArray));

        /** End Get All Courses for a Student **/

        /** Start Get All Assessments for a Student **/

        List<StudentAssessment> studentAssessments = ruleProcessorData.getStudentAssessments();
        StudentAssessment[] studentAssessmentArray = new StudentAssessment[studentAssessments.size()];
        studentAssessmentArray = studentAssessments.toArray(studentAssessmentArray);

        ParameterizedTypeReference<StudentAssessment[]> assessmentResponseType = new ParameterizedTypeReference<StudentAssessment[]>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getStudentAssessmentUrl + "/" + pen)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(assessmentResponseType)).thenReturn(Mono.just(studentAssessmentArray));

        /** End Get All Assessments for a Student **/

        /** Start Get All course Requirements **/

        CourseRequirements entity = new CourseRequirements();
        List<CourseRequirement> courseRequirementList = ruleProcessorData.getCourseRequirements();
        entity.setCourseRequirementList(courseRequirementList);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(getCourseRequirementsUrl + "/course-list")).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CourseRequirements.class)).thenReturn(Mono.just(entity));

        /** End Get All course Requirements **/

        /** Start Get All Assessment Requirements **/

        AssessmentRequirements assessmentRequirements = new AssessmentRequirements();
        List<AssessmentRequirement> assessmentRequirementList = ruleProcessorData.getAssessmentRequirements();
        assessmentRequirements.setAssessmentRequirementList(assessmentRequirementList);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(getAssessmentRequirementsUrl)).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(AssessmentRequirements.class)).thenReturn(Mono.just(assessmentRequirements));

        /** End Get All Assessment Requirements **/

        /** Start Get All Grad Letter Grades **/

        GradLetterGrades gradLetterGrades = new GradLetterGrades();
        List<GradLetterGrade> gradLetterGradeList = ruleProcessorData.getGradLetterGradeList();
        gradLetterGrades.setGradLetterGradeList(gradLetterGradeList);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/lettergrade")).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradLetterGrades.class)).thenReturn(Mono.just(gradLetterGrades));

        /** End Get All Grad Letter Grades **/

        /** Start Get All Grad Special Cases **/

        List<GradSpecialCase> gradSpecialCases = ruleProcessorData.getGradSpecialCaseList();

        ParameterizedTypeReference<List<GradSpecialCase>> responseType = new ParameterizedTypeReference<List<GradSpecialCase>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/specialcase")).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(gradSpecialCases));

        /** End Get All Grad Special Cases **/

        /** Start Get Grad Algorithm Rules from the DB **/

        List<GradAlgorithmRules> gradAlgorithmRules = ruleProcessorData.getGradAlgorithmRules();

        ParameterizedTypeReference<List<GradAlgorithmRules>> gradAlgorithmRulesResponseType = new ParameterizedTypeReference<List<GradAlgorithmRules>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getAlgorithmRulesMainGradProgramUrl, programCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(gradAlgorithmRulesResponseType)).thenReturn(Mono.just(gradAlgorithmRules));

        /** End Get Grad Algorithm Rules from the DB **/

        /** Start Get All course restrictions **/

        CourseRestrictions courseRestrictions = new CourseRestrictions();
        List<CourseRestriction> courseRestrictionList = ruleProcessorData.getCourseRestrictions();
        courseRestrictions.setCourseRestrictions(courseRestrictionList);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(getCourseRestrictionUrl + "/course-list")).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CourseRestrictions.class)).thenReturn(Mono.just(courseRestrictions));

        /** End Get All course restrictions **/

        /** Start Get all Grad Program Rules **/

        List<GradProgramRule> gradProgramRules = ruleProcessorData.getGradProgramRules();

        ParameterizedTypeReference<List<GradProgramRule>> gradProgramRulesResponseType = new ParameterizedTypeReference<List<GradProgramRule>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/programrules?programCode=" + programCode)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(gradProgramRulesResponseType)).thenReturn(Mono.just(gradProgramRules));

        /** End Get all Grad Program Rules **/

        /** Start Get all Assessments **/
        List<Assessment> assessmentList = ruleProcessorData.getAssessmentList();

        ParameterizedTypeReference<List<Assessment>> speciaalProgramResponseType = new ParameterizedTypeReference<List<Assessment>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(getAssessmentBaseUrl)).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(speciaalProgramResponseType)).thenReturn(Mono.just(assessmentList));

        /** End Get all Assessments **/

        /** Start get Student Special Program by ID **/
        String studentID = gradSearchStudent.getStudentID();

        List<GradStudentSpecialProgram> gradStudentSpecialPrograms = new ArrayList<>();

        ParameterizedTypeReference<List<GradStudentSpecialProgram>> specialProgramResponseType = new ParameterizedTypeReference<List<GradStudentSpecialProgram>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getGraduaationStatusBaseUrl + "/specialprogram/studentid/%s", studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(specialProgramResponseType)).thenReturn(Mono.just(gradStudentSpecialPrograms));

        /** End get Student Special Program by ID **/

        GradSpecialProgram gradSpecialProgram = new GradSpecialProgram();
        gradSpecialProgram.setId(UUID.randomUUID());
        gradSpecialProgram.setProgramCode(programCode);
        gradSpecialProgram.setSpecialProgramCode("BD");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/specialprograms/" + programCode + "/" + gradSpecialProgram.getSpecialProgramCode())).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradSpecialProgram.class)).thenReturn(Mono.just(gradSpecialProgram));


        /** Start Get special Grad Program Rules **/

        List<GradSpecialProgramRule> gradSpecialProgramRules = new ArrayList<>();

        ParameterizedTypeReference<List<GradSpecialProgramRule>> gradSpecialProgramRulesResponseType = new ParameterizedTypeReference<List<GradSpecialProgramRule>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(programManagementBaseUrl + "/specialprogramrules/" + programCode + "/" + gradSpecialProgram.getSpecialProgramCode())).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(gradSpecialProgramRulesResponseType)).thenReturn(Mono.just(gradSpecialProgramRules));

        /** End Get special Grad Program Rules **/

        /** Start Process Grad Algorithm Rules ***/

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(ruleEngineBaseUrl + "/" + ruleEngineRunGradAlgorithmRulesUrl)).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(RuleProcessorData.class)).thenReturn(Mono.just(ruleProcessorData));

        /** End Process Grad Algorithm Rules ***/

        /** Start gget Student Graduation Status **/

        GradAlgorithmGraduationStatus gradAlgorithmGraduationStatus = ruleProcessorData.getGradStatus();

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getGraduationStatusUrl, studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradAlgorithmGraduationStatus.class)).thenReturn(Mono.just(gradAlgorithmGraduationStatus));

        /** End gget Student Graduation Status **/

        /** Start get School**/

        School school = ruleProcessorData.getSchool();

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getSchoolByMincodeUrl, school.getMinCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        /** End get School**/

        String messageType = "NOT_GRADUATED";

        GradMessaging gradMessaging = new GradMessaging();
        gradMessaging.setProgramCode(programCode);
        gradMessaging.setGradDate("202006");
        gradMessaging.setMainMessage("This is graduation message");
        gradMessaging.setMessageType(messageType);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getCodeGradMessagesProgramCodeUrl, programCode, messageType))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradMessaging.class)).thenReturn(Mono.just(gradMessaging));

        GraduationData gradData = gradAlgorithmService.graduateStudent(pen, programCode, false, accessToken);
        assertNotNull(gradData);
        LOG.debug(">graduateStudentTest");
    }

    @Test
    public void dummyTest() {}

    protected RuleProcessorData createRuleProcessorData(String jsonPath) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
        String json = readInputStream(inputStream);
        return (RuleProcessorData)jsonTransformer.unmarshall(json, RuleProcessorData.class);
    }

    private String readInputStream(InputStream is) throws Exception {
        StringBuffer sb = new StringBuffer();
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
