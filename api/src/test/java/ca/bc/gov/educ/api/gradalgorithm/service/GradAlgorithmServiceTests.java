package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentRequirement;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentRequirements;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRequirement;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRequirements;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAlgorithmServiceTests extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradAlgorithmServiceTests.class);
    private static final String CLASS_NAME = GradAlgorithmServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradAlgorithmService gradAlgorithmService;

    @MockBean
    WebClient webClient;
    @Autowired
    private GradAlgorithmAPIConstants constants;

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
    @Value("${endpoint.gradalgorithm-api.grad-program-management-api.program_management_base.url}")
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
        /** End Get All Grad Letter Grades **/

        /** Start Get All Grad Special Cases **/
        /** End Get All Grad Special Cases **/

        /** Start Get Grad Algorithm Rules from the DB **
        /** End Get Grad Algorithm Rules from the DB **/

        /** Start Get All course restrictions **/

        /** End Get All course restrictions **/

        /** Start Get all Grad Program Rules **/

        /** End Get all Grad Program Rules **/

        /** Start Get all Assessments **/

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

        GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = ruleProcessorData.getGradStatus();

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getGraduationStatusUrl, studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradAlgorithmGraduationStudentRecord.class)).thenReturn(Mono.just(gradAlgorithmGraduationStatus));

        /** End gget Student Graduation Status **/

        /** Start get School**/

        School school = ruleProcessorData.getSchool();

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(getSchoolByMincodeUrl, school.getMinCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        /** End get School**/

        GraduationData gradData = gradAlgorithmService.graduateStudent(UUID.fromString(studentID), programCode, false, accessToken);
        assertNotNull(gradData);
        LOG.debug(">graduateStudentTest");
    }
}
