package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.junit.Assert.assertNotNull;
import java.util.*;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradAlgorithmServiceTests extends EducGradAlgorithmTestBase {

    @Autowired GradAlgorithmService gradAlgorithmService;
    @Autowired ExceptionMessage exception;
    @MockBean GradStudentService gradStudentService;
    @MockBean GradAssessmentService gradAssessmentService;
    @MockBean GradCourseService gradCourseService;
    @MockBean GradRuleProcessorService gradRuleProcessorService;
    @MockBean GradProgramService gradProgramService;
    @MockBean GradGraduationStatusService gradGraduationStatusService;
    @MockBean StudentGraduationService studentGraduationService;
    
    @MockBean GradSchoolService gradSchoolService;

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
		ruleProcessorData.setMapOptional(new HashMap<>());
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
    public void testGraduateStudent_withoptionalPrograms_FI() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_FI.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_optional_pgm.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("FI");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("FI",obj);
    	
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
		ruleProcessorData.setMapOptional(mapOptional);
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
		
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withoptionalPrograms_AD() throws Exception {
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
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("AD");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("AD",obj);

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
        ruleProcessorData.setMapOptional(mapOptional);
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
		
		
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withoptionalPrograms_BD() throws Exception {
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
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("BD");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("BD",obj);
    	
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
        ruleProcessorData.setMapOptional(mapOptional);
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
		
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withoptionalPrograms_BC() throws Exception {
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
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("BC");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("BC",obj);
    	
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
        ruleProcessorData.setMapOptional(mapOptional);
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
		
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withoptionalPrograms_CP() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-EN";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_CP.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecord.json");
    	
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_optional_pgm_cp.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("CP");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("CP",obj);

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
        ruleProcessorData.setMapOptional(mapOptional);
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
		
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, false, accessToken);
	    assertNotNull(gradData);
    }
    
    @Test
    public void testGraduateStudent_withoptionalPrograms_DD() throws Exception {
    	UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336de91d4f");
    	String pen = "127951309";
    	String gradProgram = "2018-PF";
    	String accessToken = "accessToken";
    	RuleProcessorData ruleProcessorDatas = createRuleProcessorData("json/ruleProcessorData_DD.json");
    	GradStudentAlgorithmData gradStudentAlgorithmData = createGradStudentAlgorithmData("json/gradstatus_studentrecordPF.json");
    	CourseAlgorithmData courseAlgorithmData = createCourseAlgorithmData("json/course.json");
    	AssessmentAlgorithmData assessmentAlgorithmData = createAssessmentAlgorithmData("json/assessment.json");
    	StudentGraduationAlgorithmData studentGraduationAlgorithmData = createStudentGraduationAlgorithmData("json/studentgraduation.json");
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_optional_pgm_dd.json");
    	GradAlgorithmGraduationStudentRecord gradAlgorithmGraduationStatus = createGradStatusData("json/gradstatus_PF.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(new UUID(1, 2));
    	sp.setProgramCode("2018-PF");
    	sp.setOptionalProgramCode("DD");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("DD",obj);
    	
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
        ruleProcessorData.setMapOptional(mapOptional);
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
		
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
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
		msg.setGradProjectedMessage("asdadad");
		msg.setHonourProjectedNote("asdad");
    	
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
		ruleProcessorData.setMapOptional(new HashMap<>());
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
		msg.setGradProjectedMessage("sdada");
        msg.setProgramCode(gradProgram);
        msg.setTranscriptMessageID(new UUID(1, 1));
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setProjected(true);
		ruleProcessorData.setMapOptional(new HashMap<>());
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
