package ca.bc.gov.educ.api.gradalgorithm.service;

import static org.junit.Assert.assertNotNull;
import java.util.*;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ca.bc.gov.educ.api.gradalgorithm.EducGradAlgorithmTestBase;
import reactor.core.publisher.Mono;

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
    @MockBean
	GradProgramService gradProgramService;
    @MockBean GradGraduationStatusService gradGraduationStatusService;
    @MockBean
	StudentGraduationService studentGraduationService;
	@MockBean ParallelDataFetch parallelDataFetch;
    
    @MockBean
	GradSchoolService gradSchoolService;

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


		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);


    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
    	ruleProcessorData.setStudentCourses(parallelDTO.courseAlgorithmData().getStudentCourses());
        ruleProcessorData.setCourseRestrictions(parallelDTO.courseAlgorithmData().getCourseRestrictions() != null ? parallelDTO.courseAlgorithmData().getCourseRestrictions():null);
        ruleProcessorData.setCourseRequirements(parallelDTO.courseAlgorithmData().getCourseRequirements() != null ? parallelDTO.courseAlgorithmData().getCourseRequirements():null);
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
		ruleProcessorData.setMapOptional(new HashMap<>());
		ruleProcessorData.setSchool(school);
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(parallelDTO.assessmentAlgorithmData().getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(parallelDTO.assessmentAlgorithmData().getAssessmentRequirements() != null ? parallelDTO.assessmentAlgorithmData().getAssessmentRequirements():null);
        ruleProcessorData.setAssessmentList(parallelDTO.assessmentAlgorithmData().getAssessments() != null ? parallelDTO.assessmentAlgorithmData().getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
    	sp.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("FI");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("FI",obj);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
		ruleProcessorData.setSchool(school);
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
		ruleProcessorData.setMapOptional(mapOptional);
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "FI")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_optional_pgm_AD.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
		sp.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("AD");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
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
		ruleProcessorData.setSchool(school);
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "AD")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_optional_pgm_AD.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("BD");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
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
		ruleProcessorData.setSchool(school);
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "BD")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
    	GradProgramAlgorithmData programAlgorithmData = createProgramAlgorithmData("json/program_optional_pgm_AD.json");
    	School school = createSchoolData("json/school.json");
    	
    	List<StudentOptionalProgram> gradOptionalResponseList = new ArrayList<>();
    	StudentOptionalProgram sp = new StudentOptionalProgram();
    	sp.setId(new UUID(1, 1));
    	sp.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("BC");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
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
		ruleProcessorData.setSchool(school);
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
        Mockito.when(gradStudentService.getStudentDemographics(studentID, accessToken)).thenReturn(gradStudentAlgorithmData.getGradStudent());
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradGraduationStatusService.getStudentGraduationStatus(ruleProcessorDatas.getGradStudent().getStudentID(), accessToken)).thenReturn(gradStudentAlgorithmData.getGraduationStudentRecord());
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "BC")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
    	sp.setOptionalProgramID(UUID.fromString("c732d2c0-b97c-3487-e053-98e9228e24f2"));
    	sp.setProgramCode("2018-EN");
    	sp.setOptionalProgramCode("CP");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramID(UUID.fromString("c732d2c0-b97c-3487-e053-98e9228e24f2"));
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("CP",obj);

    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
    	ruleProcessorData.setGradStatus(gradStudentAlgorithmData.getGraduationStudentRecord());
		ruleProcessorData.setSchool(school);
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
        ruleProcessorData.setMapOptional(mapOptional);
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "CP")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
    	sp.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
    	sp.setProgramCode("2018-PF");
    	sp.setOptionalProgramCode("DD");
    	sp.setOptionalProgramName("French Immersion");
    	gradOptionalResponseList.add(sp);

		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		OptionalProgramRuleProcessor obj = new OptionalProgramRuleProcessor();
		obj.setOptionalProgramGraduated(true);
		obj.setHasOptionalProgram(true);
		obj.setOptionalProgramID(UUID.fromString("c71ba15e-1cb8-ccce-e053-98e9228e1b71"));
		obj.setOptionalProgramName("French Immersion");
		obj.setOptionalProgramRules(programAlgorithmData.getOptionalProgramRules());
		mapOptional.put("DD",obj);
    	
    	RuleProcessorData ruleProcessorData = new RuleProcessorData();
    	ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
		ruleProcessorData.setSchool(school);
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
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		Mockito.when(gradGraduationStatusService.getStudentGraduationStatus(ruleProcessorDatas.getGradStudent().getStudentID(), accessToken)).thenReturn(gradAlgorithmGraduationStatus);
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradGraduationStatusService.getStudentOptionalProgramsById(studentID.toString(), accessToken,exception)).thenReturn(gradOptionalResponseList);
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "DD")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);

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
		ruleProcessorData.setSchool(school);
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
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
		ruleProcessorData.setSchool(school);
    	ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());  
    	ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        ruleProcessorData.setGradProgramRules(programAlgorithmData.getProgramRules());
        ruleProcessorData.setGradProgram(programAlgorithmData.getGradProgram());
		ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
		ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
		ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
        Mockito.when(gradStudentService.getGradStudentData(studentID,accessToken,exception)).thenReturn(gradStudentAlgorithmData);
		Mockito.when(gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception)).thenReturn(ruleProcessorDatas);
		AlgorithmDataParallelDTO parallelDTO = new AlgorithmDataParallelDTO(courseAlgorithmData,assessmentAlgorithmData);
		String schoolOfRecord = ruleProcessorDatas.getGradStudent().getSchoolOfRecord();
		Mockito.when(parallelDataFetch.fetchAlgorithmRequiredData(pen,accessToken,exception)).thenReturn(Mono.just(parallelDTO));
		Mockito.when(gradCourseService.prepareCourseDataForAlgorithm(parallelDTO.courseAlgorithmData())).thenReturn(parallelDTO.courseAlgorithmData());
		Mockito.when(gradAssessmentService.prepareAssessmentDataForAlgorithm(parallelDTO.assessmentAlgorithmData())).thenReturn(parallelDTO.assessmentAlgorithmData());
		Mockito.when(gradSchoolService.retrieveSchoolByMincode(schoolOfRecord)).thenReturn(school);
		Mockito.when(gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "")).thenReturn(programAlgorithmData);
		Mockito.when(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram)).thenReturn(studentGraduationAlgorithmData);
		GraduationData gradData = gradAlgorithmService.graduateStudent(studentID, gradProgram, true, accessToken);
	    assertNotNull(gradData);
    }
}
