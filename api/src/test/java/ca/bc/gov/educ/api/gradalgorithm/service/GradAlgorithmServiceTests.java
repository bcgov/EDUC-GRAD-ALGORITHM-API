package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.openMocks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAlgorithmServiceTests extends EducGradAlgorithmTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(GradAlgorithmServiceTests.class);
    private static final String CLASS_NAME = GradAlgorithmServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    GradAlgorithmService gradAlgorithmService;
    
    @Autowired
    private ExceptionMessage exception;

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
    
    @MockBean
    GradStudentService gradStudentService;
    @MockBean
    GradAssessmentService gradAssessmentService;
    @MockBean
    GradCourseService gradCourseService;
    @MockBean
    GradRuleProcessorService gradRuleProcessorService;
    @MockBean
    GradProgramService gradProgramService;
    @MockBean
    GradGraduationStatusService gradGraduationStatusService;
    @MockBean
    StudentGraduationService studentGraduationService;
    
    @MockBean
    GradSchoolService gradSchoolService;

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
    public void testGraduateStudent() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
    	School school = createSchoolData("json/school.json");
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withspecialPrograms_FI() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_FI.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_special_pgm.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradSpecialResponseList = new ArrayList<StudentOptionalProgram>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setSpecialProgramCode("FI");
    	sp.setSpecialProgramName("French Immersion");
    	gradSpecialResponseList.add(sp);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setGradSpecialProgramRulesFrenchImmersion(programAlgorithmData.getOptionalProgramRules());
        ruleProcessorData.setHasSpecialProgramFrenchImmersion(true);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "FI", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		
		Mockito.when(gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradSpecialResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withspecialPrograms_AD() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_AD.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradSpecialResponseList = new ArrayList<StudentOptionalProgram>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setSpecialProgramCode("AD");
    	sp.setSpecialProgramName("French Immersion");
    	gradSpecialResponseList.add(sp);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setGradSpecialProgramRulesAdvancedPlacement(programAlgorithmData.getOptionalProgramRules());
        ruleProcessorData.setHasSpecialProgramAdvancedPlacement(true);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
    	Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "AD", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		
		
		Mockito.when(gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradSpecialResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withspecialPrograms_BD() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_BD.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradSpecialResponseList = new ArrayList<StudentOptionalProgram>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setSpecialProgramCode("BD");
    	sp.setSpecialProgramName("French Immersion");
    	gradSpecialResponseList.add(sp);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setGradSpecialProgramRulesInternationalBaccalaureateBD(programAlgorithmData.getOptionalProgramRules());
        ruleProcessorData.setHasSpecialProgramInternationalBaccalaureateBD(true);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
    	Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "BD", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		
		Mockito.when(gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradSpecialResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withspecialPrograms_BC() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_BC.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradSpecialResponseList = new ArrayList<StudentOptionalProgram>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setSpecialProgramCode("BC");
    	sp.setSpecialProgramName("French Immersion");
    	gradSpecialResponseList.add(sp);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setGradSpecialProgramRulesInternationalBaccalaureateBC(programAlgorithmData.getOptionalProgramRules());
        ruleProcessorData.setHasSpecialProgramInternationalBaccalaureateBC(true);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradStudentService.getStudentDemographics(studentID, accessToken)).thenReturn(gradStudentAlgorithmData.getGradStudent());
    	Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "BC", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradGraduationStatusService.getStudentGraduationStatus(ruleProcessorDatas.getGradStudent().getStudentID(), accessToken)).thenReturn(gradStudentAlgorithmData.getGraduationStudentRecord());
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		
		Mockito.when(gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradSpecialResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withspecialPrograms_CP() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_CP.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_special_pgm_cp.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradSpecialResponseList = new ArrayList<StudentOptionalProgram>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setSpecialProgramCode("CP");
    	sp.setSpecialProgramName("French Immersion");
    	gradSpecialResponseList.add(sp);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setGradSpecialProgramRulesCareerProgram(programAlgorithmData.getOptionalProgramRules());
        ruleProcessorData.setHasSpecialProgramCareerProgram(true);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
    	Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "CP", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		
		Mockito.when(gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradSpecialResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withspecialPrograms_DD() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-PF";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_DD.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecordPF.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_special_pgm_dd.json");
    	GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = createGradStatusData("json/gradstatus_PF.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradSpecialResponseList = new ArrayList<StudentOptionalProgram>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-PF");
    	sp.setSpecialProgramCode("DD");
    	sp.setSpecialProgramName("French Immersion");
    	gradSpecialResponseList.add(sp);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setGradSpecialProgramRulesDualDogwood(programAlgorithmData.getOptionalProgramRules());
        ruleProcessorData.setHasSpecialProgramDualDogwood(true);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "DD", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradGraduationStatusService.getStudentGraduationStatus(ruleProcessorDatas.getGradStudent().getStudentID(), accessToken)).thenReturn(gradAlgorithmGraduationStatus);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		
		Mockito.when(gradGraduationStatusService.getStudentSpecialProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradSpecialResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_projected() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	String msgType = "GRADUATED";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_projected.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
    	School school = createSchoolData("json/school.json");
    	
    	TranscriptMessage msg = new TranscriptMessage();
        msg.setAdIBProgramMessage("dsada");
        msg.setCareerProgramMessage("asdsa");
        msg.setGradDateMessage("asddad");
        msg.setGradMainMessage("ASdasdad");
        msg.setHonourNote("asda");
        msg.setMessageTypeCode(msgType);
        msg.setProgramCadre("Adasd");
        msg.setProgramCode(gradProgram);
        msg.setTranscriptMessageID(new UUID(1, 1));
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setProjected(true);
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		Mockito.when(studentGraduationService.getGradMessages(gradProgram, msgType, accessToken,exception)).thenReturn(msg);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, true, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_projected_SCCP() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "SCCP";
    	String accessToken = "accessToken";
    	String msgType = "GRADUATED";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_projected.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program.json");
    	School school = createSchoolData("json/school.json");
    	
    	TranscriptMessage msg = new TranscriptMessage();
        msg.setAdIBProgramMessage("dsada");
        msg.setCareerProgramMessage("asdsa");
        msg.setGradDateMessage("asddad");
        msg.setGradMainMessage("ASdasdad");
        msg.setHonourNote("asda");
        msg.setMessageTypeCode(msgType);
        msg.setProgramCadre("Adasd");
        msg.setProgramCode(gradProgram);
        msg.setTranscriptMessageID(new UUID(1, 1));
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setProjected(true);
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception)).thenReturn(assessmentAlgorithmData);
		Mockito.when(studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception)).thenReturn(studentGraduationAlgorithmData);
		Mockito.when(gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception)).thenReturn(courseAlgorithmData);
		Mockito.when(gradProgramService.getProgramDataForAlgorithm(gradProgram, "", accessToken,exception)).thenReturn(programAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradSchoolService.getSchool(ruleProcessorDatas.getGradStudent().getSchoolOfRecord(), accessToken,exception)).thenReturn(school);
		Mockito.when(studentGraduationService.getGradMessages(gradProgram, msgType, accessToken,exception)).thenReturn(msg);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, true, accessToken);
	    assertNotNull(gradData);
    }
}
