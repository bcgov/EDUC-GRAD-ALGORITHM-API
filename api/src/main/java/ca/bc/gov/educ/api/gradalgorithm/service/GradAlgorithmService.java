package ca.bc.gov.educ.api.gradalgorithm.service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradalgorithm.dto.Assessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentRequirements;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRequirements;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRestrictions;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradMessaging;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramRule;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradRequirement;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.dto.LetterGrade;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.dto.SpecialGradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessments;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourses;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;

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
    GradStudentCourseService gradStudentCourseService;

    @Autowired
    GradStudentAssessmentService gradStudentAssessmentService;

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
    GradCodeService gradCodeService;
    
    @Autowired
    StudentGraduationService studentGraduationService;

    boolean isGraduated = true;
    HttpHeaders httpHeaders;

    public GraduationData graduateStudent(String pen, String gradProgram, boolean projected, String accessToken) {
        logger.info("\n************* New Graduation Algorithm START  ************");
        httpHeaders = APIUtils.getHeaders(accessToken);
        logger.info("**** PEN: ****" + pen.substring(5));
        logger.info("**** Grad Program: " + gradProgram);
        ruleProcessorData = new RuleProcessorData();

        //Get Student Demographics
        ruleProcessorData.setGradStudent(
                gradStudentService.getStudentDemographics(pen, accessToken));

        //Get All Courses for a Student
        List<StudentCourse> studentCourses = Arrays.asList(
                gradStudentCourseService.getAllCoursesForAStudent(pen, accessToken));
        ruleProcessorData.setStudentCourses(studentCourses);

        //Get All Assessments for a Student
        List<StudentAssessment> sAssessments = gradStudentAssessmentService.getAllAssessmentsForAStudent(pen, accessToken);
        ruleProcessorData.setStudentAssessments(sAssessments);

        //Get All course Requirements
        CourseRequirements cReq = gradCourseService.getAllCourseRequirements(studentCourses, accessToken);
        ruleProcessorData.setCourseRequirements(cReq != null ?cReq.getCourseRequirementList():null);        

        //Get All Assessment Requirements
        AssessmentRequirements aReq = gradAssessmentService.getAllAssessmentRequirements(sAssessments, accessToken);
        ruleProcessorData.setAssessmentRequirements(aReq != null ? aReq.getAssessmentRequirementList():null);

        //Get All Letter Grades,Special Case and AlgorithmRules
        StudentGraduationAlgorithmData studentGraduationAlgorithmData = studentGraduationService.getAllAlgorithmData(gradProgram, accessToken);
        ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
        ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
        ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
        

        //Get All course restrictions
        CourseRestrictions cRes = gradCourseService.getAllCourseRestrictions(studentCourses, accessToken);
        ruleProcessorData.setCourseRestrictions(cRes != null ? cRes.getCourseRestrictions():null); 

        //Get all Grad Program Rules
        List<GradProgramRule> programRulesList = gradProgramService.getProgramRules(gradProgram, accessToken);
        ruleProcessorData.setGradProgramRules(programRulesList);
        List<Assessment> assessmentList = gradAssessmentService.getAllAssessments(accessToken);
        ruleProcessorData.setAssessmentList(assessmentList);

        //Set Projected flag
        ruleProcessorData.setProjected(projected);

        //Set Special Program Flag
        ruleProcessorData =
                checkForSpecialProgram(ruleProcessorData.getGradStudent().getStudentID(), ruleProcessorData, accessToken);

        if (ruleProcessorData.isHasSpecialProgramFrenchImmersion())
            ruleProcessorData.setGradSpecialProgramRulesFrenchImmersion(
                    gradProgramService.getSpecialProgramRules(gradProgram, "FI", accessToken));

        if (ruleProcessorData.isHasSpecialProgramAdvancedPlacement())
            ruleProcessorData.setGradSpecialProgramRulesAdvancedPlacement(
                    gradProgramService.getSpecialProgramRules(gradProgram, "AD", accessToken));

        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBD())
            ruleProcessorData.setGradSpecialProgramRulesInternationalBaccalaureateBD(
                    gradProgramService.getSpecialProgramRules(gradProgram, "BD", accessToken));

        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBC())
            ruleProcessorData.setGradSpecialProgramRulesInternationalBaccalaureateBC(
                    gradProgramService.getSpecialProgramRules(gradProgram, "BC", accessToken));

        if (ruleProcessorData.isHasSpecialProgramCareerProgram())
            ruleProcessorData.setGradSpecialProgramRulesCareerProgram(
                    gradProgramService.getSpecialProgramRules(gradProgram, "CP", accessToken));

        if (ruleProcessorData.isHasSpecialProgramDualDogwood())
        	ruleProcessorData.setGradSpecialProgramRulesDualDogwood(
                    gradProgramService.getSpecialProgramRules(gradProgram, "DD", accessToken));

        //Calling Rule Processor
        ruleProcessorData = gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, accessToken);
        isGraduated = ruleProcessorData.isGraduated();
        //Populate Grad Status Details
        GradAlgorithmGraduationStatus gradStatus =
                gradGraduationStatusService.getStudentGraduationStatus(
                        ruleProcessorData.getGradStudent().getStudentID(), pen, accessToken);
        String existingProgramCompletionDate = gradStatus.getProgramCompletionDate();
        List<GradRequirement> existingNonGradReasons = null;
        try {
			GraduationData existingData = new ObjectMapper().readValue(gradStatus.getStudentGradData(), GraduationData.class);
			existingNonGradReasons = existingData.getNonGradReasons();
		} catch (JsonProcessingException e) {
			e.getMessage();
		}
        if(isGraduated) {
			if (!gradProgram.equalsIgnoreCase("SCCP")) {
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
        }

        ruleProcessorData.setGradStatus(gradStatus);
        
        //Populating Special Grad Status
        List<SpecialGradAlgorithmGraduationStatus> specialProgramStatusList = new ArrayList<>();
        if (ruleProcessorData.isHasSpecialProgramFrenchImmersion())
        	specialProgramStatusList =
                    getListOfSpecialProgramStatus(pen, gradProgram,"FI", specialProgramStatusList, accessToken);

        if (ruleProcessorData.isHasSpecialProgramCareerProgram())
        	specialProgramStatusList =
                    getListOfSpecialProgramStatus(pen, gradProgram,"CP", specialProgramStatusList, accessToken);

        if (ruleProcessorData.isHasSpecialProgramAdvancedPlacement())
        	specialProgramStatusList =
                    getListOfSpecialProgramStatus(pen, gradProgram,"AD", specialProgramStatusList, accessToken);

        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBD())
        	specialProgramStatusList =
                    getListOfSpecialProgramStatus(pen, gradProgram,"BD", specialProgramStatusList, accessToken);

        if (ruleProcessorData.isHasSpecialProgramInternationalBaccalaureateBC())
        	specialProgramStatusList =
                    getListOfSpecialProgramStatus(pen, gradProgram,"BC", specialProgramStatusList, accessToken);

        if (ruleProcessorData.isHasSpecialProgramDualDogwood())
        	specialProgramStatusList =
                    getListOfSpecialProgramStatus(pen, gradProgram,"DD", specialProgramStatusList, accessToken);
		
        ruleProcessorData.setSchool(
                gradSchoolService.getSchool(ruleProcessorData.getGradStudent().getSchoolOfRecord(), accessToken)
        );

        //Convert ruleProcessorData into GraduationData object
		graduationData.setGradStudent(ruleProcessorData.getGradStudent());
		graduationData.setGradStatus(ruleProcessorData.getGradStatus());
		graduationData.setSpecialGradStatus(specialProgramStatusList);
		graduationData.setSchool(ruleProcessorData.getSchool());
		graduationData.setStudentCourses(new StudentCourses(ruleProcessorData.getStudentCourses()));
        graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
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
        
        //This is done for Reports only grad run - Student already graduated, no change in grad message
        if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
	        if(graduationData.isGraduated()) {
	        	graduationData.setGradMessage(
	        	        getGradMessages(gradProgram, "GRADUATED", graduationData.getGradStatus().getProgramCompletionDate(),
	                            graduationData.getGradStatus().getHonoursStanding(), accessToken)
	            );
	        }else {
	        	graduationData.setGradMessage(
	        	        getGradMessages(gradProgram, "NOT_GRADUATED", graduationData.getGradStatus().getProgramCompletionDate(),
	                            graduationData.getGradStatus().getHonoursStanding(), accessToken)
	            );
	        }
        }

        logger.info("\n************* Graduation Algorithm END  ************");

        return graduationData;
    }

	
	/******************************************************************************************************************
	Utility Methods
	*******************************************************************************************************************/

    private List<SpecialGradAlgorithmGraduationStatus> getListOfSpecialProgramStatus(
            String pen, String gradProgram, String specialProgramCode,
            List<SpecialGradAlgorithmGraduationStatus> specialProgramStatusList, String accessToken) {
    	List<GradRequirement> nonGradReasons = new ArrayList<>();
    	List<GradRequirement> reqMet = new ArrayList<>();
    	SpecialGradAlgorithmGraduationStatus gradStudentSpecialAlg = new SpecialGradAlgorithmGraduationStatus();
		gradStudentSpecialAlg.setPen(pen);
		gradStudentSpecialAlg.setSpecialProgramID(
		        gradProgramService.getSpecialProgramID(gradProgram, specialProgramCode, accessToken)
        );
		gradStudentSpecialAlg.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
		
		switch(specialProgramCode) {
		case "FI":
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramFrenchImmersionGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForFrenchImmersion()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsFrenchImmersion();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsFrenchImmersion();
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "CP":
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramCareerProgramGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForCareerProgram()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsCareerProgram();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsCareerProgram();
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "DD":
			gradStudentSpecialAlg.setSpecialGraduated(ruleProcessorData.isSpecialProgramDualDogwoodGraduated());
			gradStudentSpecialAlg.setSpecialStudentCourses(new StudentCourses(ruleProcessorData.getStudentCoursesForDualDogwood()));
			gradStudentSpecialAlg.setSpecialStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessmentsForDualDogwood()));
			reqMet = ruleProcessorData.getRequirementsMetSpecialProgramsDualDogwood();
			nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsDualDogwood();
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(gradStudentSpecialAlg.getSpecialStudentCourses().getStudentCourseList(),
						gradStudentSpecialAlg.getSpecialStudentAssessments().getStudentAssessmentList()));
			}
			break;
		case "AD":
		case "BC":
		case "BD":
			gradStudentSpecialAlg.setSpecialGraduated(true);
			if (gradStudentSpecialAlg.isSpecialGraduated() && isGraduated) {
				gradStudentSpecialAlg.setSpecialProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(),
						ruleProcessorData.getStudentAssessments()));
			}
			break;
		default:
			break;
		}
		
		if(nonGradReasons != null)
			Collections.sort(nonGradReasons, Comparator.comparing(GradRequirement::getRule));
		
		gradStudentSpecialAlg.setSpecialNonGradReasons(nonGradReasons);
		if(reqMet != null)
			Collections.sort(reqMet,Comparator.comparing(GradRequirement::getRule));
		
		gradStudentSpecialAlg.setSpecialRequirementsMet(reqMet);
		
		specialProgramStatusList.add(gradStudentSpecialAlg);
		return specialProgramStatusList;
    }

    private RuleProcessorData checkForSpecialProgram(String studentID, RuleProcessorData ruleProcessorData, String accessToken) {
        List<GradStudentSpecialProgram> gradSpecialResponseList =
                gradGraduationStatusService.getStudentSpecialProgramsById(studentID, accessToken);

        if (gradSpecialResponseList == null)
            return ruleProcessorData;

        for (GradStudentSpecialProgram sp : gradSpecialResponseList) {
            if (sp.getSpecialProgramCode().equalsIgnoreCase("FI")) {
                ruleProcessorData.setHasSpecialProgramFrenchImmersion(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("AD")) {
                ruleProcessorData.setHasSpecialProgramAdvancedPlacement(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("BD")) {
                ruleProcessorData.setHasSpecialProgramInternationalBaccalaureateBD(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("BC")) {
                ruleProcessorData.setHasSpecialProgramInternationalBaccalaureateBC(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("CP")) {
                ruleProcessorData.setHasSpecialProgramCareerProgram(true);
            }
            if (sp.getSpecialProgramCode().equalsIgnoreCase("DD")) {
                ruleProcessorData.setHasSpecialProgramDualDogwood(true);
            }
        }
        return ruleProcessorData;
    }

    private String getGradMessages(String gradProgram, String msgType, String gradDate, String honours, String accessToken) {

        StringBuilder strBuilder = new StringBuilder();
		GradMessaging result = gradCodeService.getGradMessages(gradProgram, msgType, accessToken);

		if(result != null) {
			if(isGraduated) {
				if(!gradProgram.equalsIgnoreCase("SCCP")) {
					if(honours.equalsIgnoreCase("Y")) {
						strBuilder.append(String.format(result.getHonours(),gradProgram));
					}else {
						strBuilder.append(String.format(result.getMainMessage(),gradProgram));
					}
					
					strBuilder.append(System.getProperty("line.separator")).append(String.format(result.getGradDate(),formatGradDate(gradDate)));
				}else {
					strBuilder.append(String.format(result.getMainMessage(),gradProgram));
				}
			}else {
				strBuilder.append(String.format(result.getMainMessage(),gradProgram));
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