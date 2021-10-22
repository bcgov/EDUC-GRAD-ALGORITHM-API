package ca.bc.gov.educ.api.gradalgorithm.service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmOptionalStudentProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradRequirement;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.LetterGrade;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessments;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourses;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.TranscriptMessage;

@Service
public class GradAlgorithmService {

    private static final Logger logger = LoggerFactory.getLogger(GradAlgorithmService.class);

    @Autowired
    RuleProcessorData ruleProcessorData;

    @Autowired
    GraduationData graduationData;

    @Autowired
    GradStudentService gradStudentService;

    @Autowired
    GradAssessmentService gradAssessmentService;

    @Autowired
    GradCourseService gradCourseService;

    @Autowired
    GradProgramService gradProgramService;

    @Autowired
    GradGraduationStatusService gradGraduationStatusService;

    @Autowired
    GradRuleProcessorService gradRuleProcessorService;

    @Autowired
    GradSchoolService gradSchoolService;
    
    @Autowired
    StudentGraduationService studentGraduationService;

    boolean isGraduated = true;

    public GraduationData graduateStudent(UUID studentID, String gradProgram, boolean projected, String accessToken) {
        logger.info("\n************* New Graduation Algorithm START  ************");
        //Get Student Demographics
        ruleProcessorData = new RuleProcessorData();
        ExceptionMessage exception = new ExceptionMessage();
        GradStudentAlgorithmData gradStudentAlgorithmData = gradStudentService.getGradStudentData(studentID,accessToken,exception);
        GradAlgorithmGraduationStudentRecord gradStatus = new GradAlgorithmGraduationStudentRecord();
        if(gradStudentAlgorithmData != null) {
	        ruleProcessorData.setGradStudent(gradStudentAlgorithmData.getGradStudent());
	        gradStatus = gradStudentAlgorithmData.getGraduationStudentRecord();
        }
        if(exception.getExceptionName() != null) {
        	graduationData.setException(exception);
        	return graduationData;
        }
        ruleProcessorData.setGradStatus(gradStatus);
        String pen=ruleProcessorData.getGradStudent().getPen();
        logger.info("**** PEN: ****" + pen.substring(5));
        logger.info("**** Grad Program: " + gradProgram);
             

        //Get All Assessment Requirements, assessments, student assessments
        AssessmentAlgorithmData assessmentAlgorithmData = gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception);
        if(assessmentAlgorithmData != null) {
	        ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
	        ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null); 
	        ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null); 
        }
        //Get All Letter Grades,Optional Case and AlgorithmRules
        StudentGraduationAlgorithmData studentGraduationAlgorithmData = studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception);
        if(studentGraduationAlgorithmData != null) {
	        ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
	        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
	        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        }
        
        //Get Student Courses, Course Restrictions, Course Requirements
        CourseAlgorithmData courseAlgorithmData = gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception);
        if(courseAlgorithmData != null) {
	        ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
	        ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null); 
	        ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null); 
        }
        //Set Projected flag
        ruleProcessorData.setProjected(projected);

        //Set Optional Program Flag
        ruleProcessorData = checkForOptionalProgram(ruleProcessorData.getGradStudent().getStudentID(), ruleProcessorData, accessToken,exception);
        boolean studentHasOp = false;
        if (ruleProcessorData.isHasOptionalProgramFrenchImmersion()) {
        	studentHasOp = true;
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "FI", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradOptionalProgramRulesFrenchImmersion(data.getOptionalProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }

        if (ruleProcessorData.isHasOptionalProgramAdvancedPlacement()) {
        	studentHasOp = true;
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "AD", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradOptionalProgramRulesAdvancedPlacement(data.getOptionalProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }
        
        if (ruleProcessorData.isHasOptionalProgramInternationalBaccalaureateBD()) {
        	studentHasOp = true;
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "BD", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradOptionalProgramRulesInternationalBaccalaureateBD(data.getOptionalProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }
        
        if (ruleProcessorData.isHasOptionalProgramInternationalBaccalaureateBC()) {
        	studentHasOp = true;
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "BC", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradOptionalProgramRulesInternationalBaccalaureateBC(data.getOptionalProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }

		if (ruleProcessorData.isHasOptionalProgramSCCPFrenchCertificate()) {
			studentHasOp = true;
			GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "FR", accessToken,exception);
			ruleProcessorData.setGradProgramRules(data.getProgramRules());
			ruleProcessorData.setGradOptionalProgramRulesSCCPFrenchCertificate(data.getOptionalProgramRules());
			ruleProcessorData.setGradProgram(data.getGradProgram());
		}
        
        if (ruleProcessorData.isHasOptionalProgramCareerProgram()) {
        	studentHasOp = true;
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "CP", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradOptionalProgramRulesCareerProgram(data.getOptionalProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }
        
        if (ruleProcessorData.isHasOptionalProgramDualDogwood()) {
        	studentHasOp = true;
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "DD", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradOptionalProgramRulesDualDogwood(data.getOptionalProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }
        
        if(!studentHasOp) {
        	GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "", accessToken,exception);
        	ruleProcessorData.setGradProgramRules(data.getProgramRules());
        	ruleProcessorData.setGradProgram(data.getGradProgram());
        }
       
        
        
        //Calling Rule Processor
        ruleProcessorData = gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken,exception);
        if(exception.getExceptionName() != null) {
        	graduationData.setException(exception);
        	return graduationData;
        }
        
        isGraduated = ruleProcessorData.isGraduated();
        
        //Populate Grad Status Details
        String existingProgramCompletionDate = gradStatus.getProgramCompletionDate();
        List<GradRequirement> existingNonGradReasons = null;
        String existingGradMessage = null;
        try {
			if(gradStatus.getStudentGradData() != null) {
				GraduationData existingData = new ObjectMapper().readValue(gradStatus.getStudentGradData(), GraduationData.class);
				existingNonGradReasons = existingData.getNonGradReasons();
				existingGradMessage = existingData.getGradMessage();
			}
		} catch (JsonProcessingException e) {
			e.getMessage();
		}
        gradStatus.setStudentGradData(null);
        if(isGraduated) {
			if (!gradProgram.equalsIgnoreCase("SCCP") && !gradProgram.equalsIgnoreCase("NOPROG")) {
				//This is done for Reports only grad run -Student already graduated no change in graduation date
				if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
					gradStatus.setProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
				             ruleProcessorData.getStudentAssessments()));
				}
				gradStatus.setGpa(getGPA(ruleProcessorData.getStudentCourses(), ruleProcessorData.getStudentAssessments(),
				        ruleProcessorData.getLetterGradeList()));
				gradStatus.setHonoursStanding(getHonoursFlag(gradStatus.getGpa()));
			}
			
			//This is done for Reports only grad run -Student already graduated no change in graduation date
			if((existingProgramCompletionDate == null || ruleProcessorData.isProjected()) && gradStatus.getSchoolAtGrad() == null) {
				gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
	        } 
			
			if(existingProgramCompletionDate != null && (gradProgram.equalsIgnoreCase("SCCP") || gradProgram.equalsIgnoreCase("NOPROG"))) {
				gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
			}
        }

        ruleProcessorData.setGradStatus(gradStatus);
        
        //Populating Optional Grad Status
        List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList = new ArrayList<>();
        if (ruleProcessorData.isHasOptionalProgramFrenchImmersion())
        	optionalProgramStatusList =
                    getListOfOptionalProgramStatus(pen, gradProgram,"FI", optionalProgramStatusList, accessToken,exception);

        if (ruleProcessorData.isHasOptionalProgramCareerProgram())
        	optionalProgramStatusList =
                    getListOfOptionalProgramStatus(pen, gradProgram,"CP", optionalProgramStatusList, accessToken,exception);

        if (ruleProcessorData.isHasOptionalProgramAdvancedPlacement())
        	optionalProgramStatusList =
                    getListOfOptionalProgramStatus(pen, gradProgram,"AD", optionalProgramStatusList, accessToken,exception);

        if (ruleProcessorData.isHasOptionalProgramInternationalBaccalaureateBD())
        	optionalProgramStatusList =
                    getListOfOptionalProgramStatus(pen, gradProgram,"BD", optionalProgramStatusList, accessToken,exception);

        if (ruleProcessorData.isHasOptionalProgramInternationalBaccalaureateBC())
        	optionalProgramStatusList =
                    getListOfOptionalProgramStatus(pen, gradProgram,"BC", optionalProgramStatusList, accessToken,exception);

		if (ruleProcessorData.isHasOptionalProgramSCCPFrenchCertificate())
			optionalProgramStatusList =
					getListOfOptionalProgramStatus(pen, gradProgram,"FR", optionalProgramStatusList, accessToken,exception);

        if (ruleProcessorData.isHasOptionalProgramDualDogwood())
        	optionalProgramStatusList =
                    getListOfOptionalProgramStatus(pen, gradProgram,"DD", optionalProgramStatusList, accessToken,exception);
		
        ruleProcessorData.setSchool(
                gradSchoolService.getSchool(ruleProcessorData.getGradStudent().getSchoolOfRecord(), accessToken,exception)
        );

        //Convert ruleProcessorData into GraduationData object
		graduationData.setGradStudent(ruleProcessorData.getGradStudent());
		graduationData.setGradStatus(ruleProcessorData.getGradStatus());
		graduationData.setOptionalGradStatus(optionalProgramStatusList);
		graduationData.setSchool(ruleProcessorData.getSchool());
		graduationData.setStudentCourses(new StudentCourses(ruleProcessorData.getStudentCourses()));
        graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));

        if(ruleProcessorData.getNonGradReasons() != null)
        	Collections.sort(ruleProcessorData.getNonGradReasons(), Comparator.comparing(GradRequirement::getRule));
        
        //This is done for Reports only grad run
        if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
        	graduationData.setNonGradReasons(ruleProcessorData.getNonGradReasons());
        }
        
        if(existingNonGradReasons != null && !existingNonGradReasons.isEmpty() && ruleProcessorData.isProjected()) {
        	for(GradRequirement gR:existingNonGradReasons) {
        		boolean ruleExists = false;
        		if(graduationData.getNonGradReasons() != null) {
        			ruleExists= graduationData.getNonGradReasons().stream().anyMatch(nGR -> nGR.getRule().compareTo(gR.getRule())==0);
        		}
    			if(!ruleExists && ruleProcessorData.getRequirementsMet() != null) {        			
	        		ruleProcessorData.getRequirementsMet().stream().filter(rM -> rM.getRule().compareTo(gR.getRule()) == 0).forEach(rM -> rM.setProjected(true));
        		}
        	}
        }
        
        if(ruleProcessorData.getRequirementsMet() != null)
        	Collections.sort(ruleProcessorData.getRequirementsMet(), Comparator.comparing(GradRequirement::getRule));

        graduationData.setRequirementsMet(ruleProcessorData.getRequirementsMet());
        graduationData.setGraduated(ruleProcessorData.isGraduated());
        if(graduationData.getGradStatus().getProgramCompletionDate() == null && gradProgram.equalsIgnoreCase("SCCP")) {
        	graduationData.setGraduated(false);
        }
        //This is done for Reports only grad run - Student already graduated, no change in grad message
        if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
	        if(graduationData.isGraduated()) {
	        	graduationData.setGradMessage(
	        	        getGradMessages(gradProgram, "GRADUATED", graduationData.getGradStatus().getProgramCompletionDate(),
	                            graduationData.getGradStatus().getHonoursStanding(),ruleProcessorData.getGradProgram().getProgramName(),accessToken,exception)
	            );
	        }else {
	        	graduationData.setGradMessage(
	        	        getGradMessages(gradProgram, "NOT_GRADUATED", graduationData.getGradStatus().getProgramCompletionDate(),
	                            graduationData.getGradStatus().getHonoursStanding(),ruleProcessorData.getGradProgram().getProgramName(), accessToken,exception)
	            );
	        }
        }
        if(existingProgramCompletionDate != null && (gradProgram.equalsIgnoreCase("SCCP") || gradProgram.equalsIgnoreCase("NOPROG"))) {
        	graduationData.setGradMessage(
        	        getGradMessages(gradProgram, "GRADUATED", graduationData.getGradStatus().getProgramCompletionDate(),
                            graduationData.getGradStatus().getHonoursStanding(),ruleProcessorData.getGradProgram().getProgramName(), accessToken,exception)
            );
        }
        if(existingGradMessage != null && existingProgramCompletionDate != null && !gradProgram.equalsIgnoreCase("SCCP") && !gradProgram.equalsIgnoreCase("NOPROG")) {
        	graduationData.setGradMessage(existingGradMessage);
        }
        
        if(exception.getExceptionName() != null) {
        	graduationData.setException(exception);
        	return graduationData;
        }
        logger.info("\n************* Graduation Algorithm END  ************");
		
        return graduationData;
    }

	
	/******************************************************************************************************************
	Utility Methods
	*******************************************************************************************************************/

    private List<GradAlgorithmOptionalStudentProgram> getListOfOptionalProgramStatus(
            String pen, String gradProgram, String optionalProgramCode,
            List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, String accessToken,ExceptionMessage exception) {
    	List<GradRequirement> nonGradReasons = new ArrayList<>();
    	List<GradRequirement> reqMet = new ArrayList<>();
    	GradAlgorithmOptionalStudentProgram gradStudentOptionalAlg = new GradAlgorithmOptionalStudentProgram();
		gradStudentOptionalAlg.setPen(pen);
		gradStudentOptionalAlg.setOptionalProgramID(gradProgramService.getOptionalProgramID(gradProgram, optionalProgramCode, accessToken,exception));
		gradStudentOptionalAlg.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
		
		switch(optionalProgramCode) {
		case "FI":
			gradStudentOptionalAlg.setOptionalGraduated(ruleProcessorData.isOptionalProgramFrenchImmersionGraduated());
			gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForFrenchImmersion()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessmentsForFrenchImmersion()));
			reqMet = ruleProcessorData.getRequirementsMetOptionalProgramsFrenchImmersion();
			nonGradReasons = ruleProcessorData.getNonGradReasonsOptionalProgramsFrenchImmersion();
			if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
				gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList(),
						gradStudentOptionalAlg.getOptionalStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "CP":
			if(!gradProgram.equalsIgnoreCase("1950")) {
				gradStudentOptionalAlg.setOptionalGraduated(ruleProcessorData.isOptionalProgramCareerProgramGraduated());
				gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForCareerProgram()));
				gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments());
				reqMet = ruleProcessorData.getRequirementsMetOptionalProgramsCareerProgram();
				nonGradReasons = ruleProcessorData.getNonGradReasonsOptionalProgramsCareerProgram();
				if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
					gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList(),
							gradStudentOptionalAlg.getOptionalStudentAssessments().getStudentAssessmentList()));
				}
			}else {
				gradStudentOptionalAlg.setOptionalGraduated(true);
				gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments());
				gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses());
				if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
					gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
							ruleProcessorData.getStudentAssessments()));
				}
			}
			break;
		case "DD":
			gradStudentOptionalAlg.setOptionalGraduated(ruleProcessorData.isOptionalProgramDualDogwoodGraduated());
			gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForDualDogwood()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessmentsForDualDogwood()));
			reqMet = ruleProcessorData.getRequirementsMetOptionalProgramsDualDogwood();
			nonGradReasons = ruleProcessorData.getNonGradReasonsOptionalProgramsDualDogwood();
			if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
				gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList(),
						gradStudentOptionalAlg.getOptionalStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "AD":
		case "BC":
		case "BD":
		case "FR":
			gradStudentOptionalAlg.setOptionalGraduated(true);
			gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments());
			gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses());
			if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
				gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
						ruleProcessorData.getStudentAssessments()));
			}
			break;
		default:
			break;
		}
		
		if(nonGradReasons != null)
			Collections.sort(nonGradReasons, Comparator.comparing(GradRequirement::getRule));
		
		gradStudentOptionalAlg.setOptionalNonGradReasons(nonGradReasons);
		if(reqMet != null)
			Collections.sort(reqMet,Comparator.comparing(GradRequirement::getRule));
		
		gradStudentOptionalAlg.setOptionalRequirementsMet(reqMet);
		
		optionalProgramStatusList.add(gradStudentOptionalAlg);
		return optionalProgramStatusList;
    }

    private RuleProcessorData checkForOptionalProgram(String studentID, RuleProcessorData ruleProcessorData, String accessToken,ExceptionMessage exception) {
        List<StudentOptionalProgram> gradOptionalResponseList = gradGraduationStatusService.getStudentOptionalProgramsById(studentID, accessToken,exception);

        if (gradOptionalResponseList.isEmpty())
            return ruleProcessorData;

        for (StudentOptionalProgram sp : gradOptionalResponseList) {
            if (sp.getOptionalProgramCode().equalsIgnoreCase("FI")) {
                ruleProcessorData.setHasOptionalProgramFrenchImmersion(true);
            }
            if (sp.getOptionalProgramCode().equalsIgnoreCase("AD")) {
                ruleProcessorData.setHasOptionalProgramAdvancedPlacement(true);
            }
            if (sp.getOptionalProgramCode().equalsIgnoreCase("BD")) {
                ruleProcessorData.setHasOptionalProgramInternationalBaccalaureateBD(true);
            }
            if (sp.getOptionalProgramCode().equalsIgnoreCase("BC")) {
                ruleProcessorData.setHasOptionalProgramInternationalBaccalaureateBC(true);
            }
			if (sp.getOptionalProgramCode().equalsIgnoreCase("FR")) {
				ruleProcessorData.setHasOptionalProgramSCCPFrenchCertificate(true);
			}
            if (sp.getOptionalProgramCode().equalsIgnoreCase("CP")) {
                ruleProcessorData.setHasOptionalProgramCareerProgram(true);
            }
            if (sp.getOptionalProgramCode().equalsIgnoreCase("DD")) {
                ruleProcessorData.setHasOptionalProgramDualDogwood(true);
            }
        }
        return ruleProcessorData;
    }

    private String getGradMessages(String gradProgram, String msgType, String gradDate, String honours, String programName, String accessToken,ExceptionMessage exception) {

        StringBuilder strBuilder = new StringBuilder();
		TranscriptMessage result = studentGraduationService.getGradMessages(gradProgram, msgType, accessToken,exception);

		if(result != null) {
			if(isGraduated) {
				if(!gradProgram.equalsIgnoreCase("SCCP")) {
					if(honours.equalsIgnoreCase("Y")) {
						strBuilder.append(String.format(result.getHonourNote(),programName));
					}else {
						strBuilder.append(String.format(result.getGradMainMessage(),programName));
					}
					
					strBuilder.append(System.getProperty("line.separator")).append(String.format(result.getGradDateMessage(),formatGradDate(gradDate)));
				}else {
					strBuilder.append(String.format(result.getGradMainMessage(),programName));
				}
			}else {
				strBuilder.append(String.format(result.getGradMainMessage(),programName));
			}
	        return strBuilder.toString();
		}
		return null;
	}
    
    private String formatGradDate(String gradDate) {
    	String monthName = null;
    	LocalDate currentDate = LocalDate.parse(gradDate);
        Month month = currentDate.getMonth(); 
        int year = currentDate.getYear(); 
        monthName = month.getDisplayName(TextStyle.FULL,Locale.ENGLISH) +" "+ year;
        return monthName;
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
                          List<LetterGrade> letterGradesList) {

        studentCourseList = studentCourseList.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
        float totalCredits = studentCourseList.stream().filter(StudentCourse::isUsed).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
        float acquiredCredits = 0;
        String tempGpaMV =null;

        for (StudentCourse sc : studentCourseList) {
            tempGpaMV = "0";
            String completedCourseGrade = sc.getCompletedCourseLetterGrade() != null ? sc.getCompletedCourseLetterGrade():"";
            LetterGrade letterGrade = letterGradesList
                    .stream()
                    .filter(lg -> lg.getGrade().compareToIgnoreCase(completedCourseGrade) == 0)
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
}
