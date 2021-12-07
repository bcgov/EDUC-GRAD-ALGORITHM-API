package ca.bc.gov.educ.api.gradalgorithm.service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private static final String SCCP = "SCCP";
	private static final String NOPROGRAM = "NOPROG";

    public GraduationData graduateStudent(UUID studentID, String gradProgram, boolean projected, String accessToken) {
        logger.info("\n************* New Graduation Algorithm START  ************");
        //Get Student Demographics
        ruleProcessorData = new RuleProcessorData();
		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		ruleProcessorData.setMapOptional(mapOptional);
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
        logger.info("**** PEN: **** {}",pen != null ? pen.substring(5):"Not Found");
        logger.info("**** Grad Program: {}",gradProgram);
        //Get All Assessment Requirements, assessments, student assessments
		setCourseAssessmentDataForAlgorithm(pen,accessToken,exception);
        //Get All Letter Grades,Optional Case and AlgorithmRules
		setAlgorithmSupportData(gradProgram,accessToken,exception);
        //Set Projected flag
        ruleProcessorData.setProjected(projected);

        //Set Optional Program Flag
		checkForOptionalProgram(ruleProcessorData.getGradStudent().getStudentID(), ruleProcessorData, accessToken,exception);
		manageProgramRules(gradProgram,accessToken,exception);

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
		boolean checkSCCPNOPROG = existingProgramCompletionDate != null && (gradProgram.equalsIgnoreCase(SCCP) || gradProgram.equalsIgnoreCase(NOPROGRAM));
		if(isGraduated) {
			setGradStatusAlgorithmResponse(gradProgram,existingProgramCompletionDate,gradStatus,checkSCCPNOPROG);
        }
        ruleProcessorData.setGradStatus(gradStatus);

        //Populating Optional Grad Status
        List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList = new ArrayList<>();
        Map<String,OptionalProgramRuleProcessor> mapOption = ruleProcessorData.getMapOptional();
		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOption.entrySet()) {
			String optionalProgramCode = entry.getKey();
			OptionalProgramRuleProcessor obj = entry.getValue();
			getListOfOptionalProgramStatus(pen, gradProgram, optionalProgramCode, obj, optionalProgramStatusList, accessToken, exception);
		}
        ruleProcessorData.setSchool(
                gradSchoolService.getSchool(ruleProcessorData.getGradStudent().getSchoolOfRecord(), accessToken,exception)
        );

        //Convert ruleProcessorData into GraduationData object
		convertRuleProcessorToGraduationData(optionalProgramStatusList,existingProgramCompletionDate,existingNonGradReasons,gradProgram);
        //This is done for Reports only grad run - Student already graduated, no change in grad message
        processGradMessages(existingProgramCompletionDate,checkSCCPNOPROG,existingGradMessage,gradProgram,accessToken,exception);


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
	private void getListOfOptionalProgramStatus(
			String pen, String gradProgram, String optionalProgramCode,OptionalProgramRuleProcessor obj,
			List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, String accessToken,ExceptionMessage exception) {
		List<GradRequirement> nonGradReasons = new ArrayList<>();
		List<GradRequirement> reqMet = new ArrayList<>();
		GradAlgorithmOptionalStudentProgram gradStudentOptionalAlg = new GradAlgorithmOptionalStudentProgram();
		gradStudentOptionalAlg.setPen(pen);
		gradStudentOptionalAlg.setOptionalProgramID(gradProgramService.getOptionalProgramID(gradProgram, optionalProgramCode, accessToken,exception));
		gradStudentOptionalAlg.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
		gradStudentOptionalAlg.setOptionalGraduated(obj.isOptionalProgramGraduated());

		if(obj.getOptionalProgramRules().isEmpty()){
			gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses(new ArrayList<>()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments(new ArrayList<>()));
		}else {
			gradStudentOptionalAlg.setOptionalStudentCourses(obj.getStudentCoursesOptionalProgram() != null ? new StudentCourses(obj.getStudentCoursesOptionalProgram()) : new StudentCourses(new ArrayList<>()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(obj.getStudentAssessmentsOptionalProgram() != null ? new StudentAssessments(obj.getStudentAssessmentsOptionalProgram()) : new StudentAssessments(new ArrayList<>()));
		}
		if(obj.getRequirementsMetOptionalProgram() != null) {
			reqMet = obj.getRequirementsMetOptionalProgram();
			reqMet.sort(Comparator.comparing(GradRequirement::getRule));
		}
		if(obj.getNonGradReasonsOptionalProgram() != null) {
			nonGradReasons = obj.getNonGradReasonsOptionalProgram();
			nonGradReasons.sort(Comparator.comparing(GradRequirement::getRule));
		}

		if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
			if (!gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList().isEmpty()) {
				gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList()));
			} else {
				gradStudentOptionalAlg.setOptionalProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses()));
			}
		}
		gradStudentOptionalAlg.setOptionalNonGradReasons(nonGradReasons);
		gradStudentOptionalAlg.setOptionalRequirementsMet(reqMet);
		optionalProgramStatusList.add(gradStudentOptionalAlg);
	}

	private void checkForOptionalProgram(String studentID, RuleProcessorData ruleProcessorData, String accessToken,ExceptionMessage exception) {
		List<StudentOptionalProgram> gradOptionalResponseList = gradGraduationStatusService.getStudentOptionalProgramsById(studentID, accessToken,exception);
		if (!gradOptionalResponseList.isEmpty()) {
			Map<String, OptionalProgramRuleProcessor> mapOpt = ruleProcessorData.getMapOptional();
			for (StudentOptionalProgram sp : gradOptionalResponseList) {
				OptionalProgramRuleProcessor opRulePro = new OptionalProgramRuleProcessor();
				opRulePro.setHasOptionalProgram(true);
				opRulePro.setOptionalProgramGraduated(true);
				mapOpt.put(sp.getOptionalProgramCode(), opRulePro);
			}
			ruleProcessorData.setMapOptional(mapOpt);
		}
	}

    private String getGradMessages(GradMessageRequest gradMessageRequest,String accessToken,ExceptionMessage exception) {

        StringBuilder strBuilder = new StringBuilder();
		TranscriptMessage result = studentGraduationService.getGradMessages(gradMessageRequest.getGradProgram(), gradMessageRequest.getMsgType(), accessToken,exception);
		if(result != null) {
			if(isGraduated) {
				if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
					if(gradMessageRequest.getHonours().equalsIgnoreCase("Y")) {
						getHonoursMessageForProjected(gradMessageRequest,strBuilder,result);
					}else {
						getMessageForProjected(gradMessageRequest,strBuilder,result);
					}
					strBuilder.append(System.getProperty("line.separator")).append(String.format(result.getGradDateMessage(),formatGradDate(gradMessageRequest.getGradDate())));
				}else {
					strBuilder.append(String.format(result.getGradMainMessage(),gradMessageRequest.getProgramName()));
				}
			}else {
				getMessageForProjected(gradMessageRequest,strBuilder,result);
			}
	        return strBuilder.toString();
		}
		return null;
	}

	private void getHonoursMessageForProjected(GradMessageRequest gradMessageRequest,StringBuilder strBuilder,TranscriptMessage result) {
		if(gradMessageRequest.isProjected()) {
			strBuilder.append(String.format(result.getHonourProjectedNote(), gradMessageRequest.getProgramName()));
		}else {
			strBuilder.append(String.format(result.getHonourNote(), gradMessageRequest.getProgramName()));
		}
	}

	private void getMessageForProjected(GradMessageRequest gradMessageRequest,StringBuilder strBuilder,TranscriptMessage result) {
		if(gradMessageRequest.isProjected()) {
			strBuilder.append(String.format(result.getGradProjectedMessage(), gradMessageRequest.getProgramName()));
		}else {
			strBuilder.append(String.format(result.getGradMainMessage(),gradMessageRequest.getProgramName()));
		}
	}
    
    private String formatGradDate(String gradDate) {
    	LocalDate currentDate = LocalDate.parse(gradDate);
        Month month = currentDate.getMonth(); 
        int year = currentDate.getYear();
        return month.getDisplayName(TextStyle.FULL,Locale.ENGLISH) +" "+ year;
    }

    private String getGradDate(List<StudentCourse> studentCourses) {

        Date gradDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            gradDate = dateFormat.parse("1700/01/01");
        } catch (ParseException e) {
            logger.debug(e.getMessage());
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
				logger.debug(e.getMessage());
            }
        }
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(gradDate);
    }

    private String getGPA(List<StudentCourse> studentCourseList,List<LetterGrade> letterGradesList) {

        studentCourseList = studentCourseList.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
        float totalCredits = studentCourseList.stream().filter(StudentCourse::isUsed).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
        float acquiredCredits = 0;
        String tempGpaMV;

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

            logger.debug("Letter Grade: {} | GPA Mark Value: {} | Acquired Credits: {} | Total Credits: {}", letterGrade,gpaMarkValue,acquiredCredits,totalCredits);
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

	private void setCourseAssessmentDataForAlgorithm(String pen,String accessToken,ExceptionMessage exception) {
		CourseAlgorithmData courseAlgorithmData = gradCourseService.getCourseDataForAlgorithm(pen, accessToken,exception);
		if(courseAlgorithmData != null) {
			ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
			ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null);
			ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null);
		}

		AssessmentAlgorithmData assessmentAlgorithmData = gradAssessmentService.getAssessmentDataForAlgorithm(pen, accessToken,exception);
		if(assessmentAlgorithmData != null) {
			ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
			ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null);
			ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
		}
	}

	private void setAlgorithmSupportData(String gradProgram,String accessToken,ExceptionMessage exception) {
		StudentGraduationAlgorithmData studentGraduationAlgorithmData = studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception);
		if(studentGraduationAlgorithmData != null) {
			ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
			ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
			ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
		}
	}

	private void manageProgramRules(String gradProgram,String accessToken,ExceptionMessage exception) {
		Map<String,OptionalProgramRuleProcessor> mapOpt = ruleProcessorData.getMapOptional();
		boolean studentHasOp = mapOpt.size() > 0;
		mapOpt.forEach((k,v) ->{
			GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, k, accessToken,exception);
			ruleProcessorData.setGradProgramRules(data.getProgramRules());
			v.setOptionalProgramRules(data.getOptionalProgramRules());
			ruleProcessorData.setGradProgram(data.getGradProgram());
		});
		if(!studentHasOp) {
			GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "", accessToken,exception);
			ruleProcessorData.setGradProgramRules(data.getProgramRules());
			ruleProcessorData.setGradProgram(data.getGradProgram());
		}
	}

	private void setGradStatusAlgorithmResponse(String gradProgram,String existingProgramCompletionDate,GradAlgorithmGraduationStudentRecord gradStatus,boolean checkSCCPNOPROG) {
		if (!gradProgram.equalsIgnoreCase(SCCP) && !gradProgram.equalsIgnoreCase(NOPROGRAM)) {
			//This is done for Reports only grad run -Student already graduated no change in graduation date
			if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
				gradStatus.setProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses()));
			}
			gradStatus.setGpa(getGPA(ruleProcessorData.getStudentCourses(),ruleProcessorData.getLetterGradeList()));
			gradStatus.setHonoursStanding(getHonoursFlag(gradStatus.getGpa()));
		}

		//This is done for Reports only grad run -Student already graduated no change in graduation date
		if((existingProgramCompletionDate == null || ruleProcessorData.isProjected()) && gradStatus.getSchoolAtGrad() == null) {
			gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
		}

		if(checkSCCPNOPROG) {
			gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
		}
	}

	private void convertRuleProcessorToGraduationData(List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, String existingProgramCompletionDate, List<GradRequirement> existingNonGradReasons, String gradProgram) {
		graduationData.setGradStudent(ruleProcessorData.getGradStudent());
		graduationData.setGradStatus(ruleProcessorData.getGradStatus());
		graduationData.setOptionalGradStatus(optionalProgramStatusList);
		graduationData.setSchool(ruleProcessorData.getSchool());
		graduationData.setStudentCourses(new StudentCourses(ruleProcessorData.getStudentCourses()));
		graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));

		if(ruleProcessorData.getNonGradReasons() != null)
			ruleProcessorData.getNonGradReasons().sort(Comparator.comparing(GradRequirement::getRule));

		//This is done for Reports only grad run
		if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
			graduationData.setNonGradReasons(ruleProcessorData.getNonGradReasons());
		}
		processExistingNonGradReason(existingNonGradReasons);
		if(ruleProcessorData.getRequirementsMet() != null)
			ruleProcessorData.getRequirementsMet().sort(Comparator.comparing(GradRequirement::getRule));

		graduationData.setRequirementsMet(ruleProcessorData.getRequirementsMet());
		graduationData.setGraduated(ruleProcessorData.isGraduated());
		if(graduationData.getGradStatus().getProgramCompletionDate() == null && gradProgram.equalsIgnoreCase(SCCP)) {
			graduationData.setGraduated(false);
		}
	}

	private void processExistingNonGradReason(List<GradRequirement> existingNonGradReasons) {
		if(existingNonGradReasons != null && !existingNonGradReasons.isEmpty() && ruleProcessorData.isProjected()) {
			for (GradRequirement gR : existingNonGradReasons) {
				boolean ruleExists = false;
				if (graduationData.getNonGradReasons() != null) {
					ruleExists = graduationData.getNonGradReasons().stream().anyMatch(nGR -> nGR.getRule().compareTo(gR.getRule()) == 0);
				}
				if (!ruleExists && ruleProcessorData.getRequirementsMet() != null) {
					ruleProcessorData.getRequirementsMet().stream().filter(rM -> rM.getRule().compareTo(gR.getRule()) == 0).forEach(rM -> rM.setProjected(true));
				}
			}
		}
	}

	private void processGradMessages(String existingProgramCompletionDate, boolean checkSCCPNOPROG, String existingGradMessage, String gradProgram,String accessToken,ExceptionMessage exception) {
		if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
			GradMessageRequest gradMessageRequest = GradMessageRequest.builder()
					.gradProgram(gradProgram).gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding()).programName(ruleProcessorData.getGradProgram().getProgramName()).projected(ruleProcessorData.isProjected())
					.build();
			if(graduationData.isGraduated()) {
				gradMessageRequest.setMsgType("GRADUATED");
			}else {
				gradMessageRequest.setMsgType("NOT_GRADUATED");
			}
			graduationData.setGradMessage(getGradMessages(gradMessageRequest,accessToken,exception));
		}
		if(checkSCCPNOPROG) {
			GradMessageRequest gradMessageRequest = GradMessageRequest.builder()
					.gradProgram(gradProgram).msgType("GRADUATED").gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding()).programName(ruleProcessorData.getGradProgram().getProgramName()).projected(ruleProcessorData.isProjected())
					.build();
			graduationData.setGradMessage(getGradMessages(gradMessageRequest,accessToken,exception));
		}
		if(existingGradMessage != null && existingProgramCompletionDate != null && !gradProgram.equalsIgnoreCase(SCCP) && !gradProgram.equalsIgnoreCase(NOPROGRAM)) {
			graduationData.setGradMessage(existingGradMessage);
		}
	}
}
