package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradAlgorithmService {

    private static final Logger logger = LoggerFactory.getLogger(GradAlgorithmService.class);

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
		RuleProcessorData ruleProcessorData = new RuleProcessorData();
		GraduationData graduationData = new GraduationData();
		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		ruleProcessorData.setMapOptional(mapOptional);
        ExceptionMessage exception = new ExceptionMessage();
        GradStudentAlgorithmData gradStudentAlgorithmData = gradStudentService.getGradStudentData(studentID,accessToken,exception);
        GradAlgorithmGraduationStudentRecord gradStatus = new GradAlgorithmGraduationStudentRecord();
        if(gradStudentAlgorithmData != null) {
			ruleProcessorData.setCpList(gradStudentAlgorithmData.getStudentCareerProgramList());
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
		setCourseAssessmentDataForAlgorithm(pen,accessToken,exception,ruleProcessorData);
        //Get All Letter Grades,Optional Case and AlgorithmRules
		setAlgorithmSupportData(gradProgram,accessToken,exception,ruleProcessorData);
        //Set Projected flag
        ruleProcessorData.setProjected(projected);

        //Set Optional Program Flag
		checkForOptionalProgram(ruleProcessorData.getGradStudent().getStudentID(), ruleProcessorData, accessToken,exception);
		manageProgramRules(gradProgram,accessToken,exception,ruleProcessorData);

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
			setGradStatusAlgorithmResponse(gradProgram,existingProgramCompletionDate,gradStatus,checkSCCPNOPROG,ruleProcessorData);
        }
        ruleProcessorData.setGradStatus(gradStatus);

        //Populating Optional Grad Status
        List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList = new ArrayList<>();
        Map<String,OptionalProgramRuleProcessor> mapOption = ruleProcessorData.getMapOptional();
		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOption.entrySet()) {
			String optionalProgramCode = entry.getKey();
			OptionalProgramRuleProcessor obj = entry.getValue();
			getListOfOptionalProgramStatus(gradProgram, optionalProgramCode, obj, optionalProgramStatusList, accessToken, exception,ruleProcessorData);
		}
        ruleProcessorData.setSchool(
                gradSchoolService.getSchool(ruleProcessorData.getGradStudent().getSchoolOfRecord(), accessToken,exception)
        );

        //Convert ruleProcessorData into GraduationData object
		convertRuleProcessorToGraduationData(optionalProgramStatusList,existingProgramCompletionDate,existingNonGradReasons,gradProgram,ruleProcessorData,graduationData);
        //This is done for Reports only grad run - Student already graduated, no change in grad message
        ExistingDataSupport existingDataSupport = ExistingDataSupport.builder().existingProgramCompletionDate(existingProgramCompletionDate).existingGradMessage(existingGradMessage).gradProgam(gradProgram).build();
		processGradMessages(checkSCCPNOPROG,existingDataSupport,accessToken,exception,mapOption,ruleProcessorData,graduationData);


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
	private void getListOfOptionalProgramStatus(String gradProgram, String optionalProgramCode,OptionalProgramRuleProcessor obj,
			List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, String accessToken,ExceptionMessage exception, RuleProcessorData ruleProcessorData) {
		List<GradRequirement> nonGradReasons = new ArrayList<>();
		List<GradRequirement> reqMet = new ArrayList<>();
		GradAlgorithmOptionalStudentProgram gradStudentOptionalAlg = new GradAlgorithmOptionalStudentProgram();
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
				opRulePro.setOptionalProgramName(sp.getOptionalProgramName());
				mapOpt.put(sp.getOptionalProgramCode(), opRulePro);
			}
			ruleProcessorData.setMapOptional(mapOpt);
		}
	}

    private String getGradMessages(GradMessageRequest gradMessageRequest,String accessToken,ExceptionMessage exception,Map<String,OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {

        StringBuilder strBuilder = new StringBuilder();
		TranscriptMessage result = studentGraduationService.getGradMessages(gradMessageRequest.getGradProgram(), gradMessageRequest.getMsgType(), accessToken,exception);
		if(result != null) {
			if(isGraduated) {
				processMessageForGraduatedStudent(gradMessageRequest,strBuilder,result,mapOptional,ruleProcessorData);
			}else {
				processMessageForUnGraduatedStudent(gradMessageRequest,strBuilder,result,mapOptional,ruleProcessorData);
			}
	        return strBuilder.toString();
		}
		return null;
	}

	private void processMessageForUnGraduatedStudent(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result, Map<String, OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {
		getMessageForProjected(gradMessageRequest,strBuilder,result);
		if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
			createCompleteGradMessage(strBuilder,result,mapOptional,ruleProcessorData);
		}
	}
	private void processMessageForGraduatedStudent(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result, Map<String, OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {
		if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
			if(gradMessageRequest.getHonours().equalsIgnoreCase("Y")) {
				getHonoursMessageForProjected(gradMessageRequest,strBuilder,result);
			}else {
				getMessageForProjected(gradMessageRequest,strBuilder,result);
			}
			strBuilder.append(" ").append(String.format(result.getGradDateMessage(),formatGradDate(gradMessageRequest.getGradDate())));
			strBuilder.append(" ");
			createCompleteGradMessage(strBuilder,result,mapOptional,ruleProcessorData);
		}else {
			getMessageForProjected(gradMessageRequest,strBuilder,result);
		}
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

	private void setCourseAssessmentDataForAlgorithm(String pen,String accessToken,ExceptionMessage exception, RuleProcessorData ruleProcessorData) {
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

	private void setAlgorithmSupportData(String gradProgram, String accessToken, ExceptionMessage exception, RuleProcessorData ruleProcessorData) {
		StudentGraduationAlgorithmData studentGraduationAlgorithmData = studentGraduationService.getAllAlgorithmData(gradProgram, accessToken,exception);
		if(studentGraduationAlgorithmData != null) {
			ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
			ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
			ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
		}
	}

	private void manageProgramRules(String gradProgram, String accessToken, ExceptionMessage exception, RuleProcessorData ruleProcessorData) {
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

	private void setGradStatusAlgorithmResponse(String gradProgram, String existingProgramCompletionDate, GradAlgorithmGraduationStudentRecord gradStatus, boolean checkSCCPNOPROG, RuleProcessorData ruleProcessorData) {
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

	private void convertRuleProcessorToGraduationData(List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, String existingProgramCompletionDate, List<GradRequirement> existingNonGradReasons, String gradProgram, RuleProcessorData ruleProcessorData,GraduationData graduationData) {
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
		processExistingNonGradReason(existingNonGradReasons,ruleProcessorData,graduationData);
		if(ruleProcessorData.getRequirementsMet() != null)
			ruleProcessorData.getRequirementsMet().sort(Comparator.comparing(GradRequirement::getRule));

		graduationData.setRequirementsMet(ruleProcessorData.getRequirementsMet());
		graduationData.setGraduated(ruleProcessorData.isGraduated());
		if(graduationData.getGradStatus().getProgramCompletionDate() == null && gradProgram.equalsIgnoreCase(SCCP)) {
			graduationData.setGraduated(false);
		}
	}

	private void processExistingNonGradReason(List<GradRequirement> existingNonGradReasons,RuleProcessorData ruleProcessorData,GraduationData graduationData) {
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

	private void processGradMessages(boolean checkSCCPNOPROG, ExistingDataSupport existingDataSupport, String accessToken, ExceptionMessage exception, Map<String, OptionalProgramRuleProcessor> mapOption,RuleProcessorData ruleProcessorData,GraduationData graduationData) {
		if(existingDataSupport.getExistingProgramCompletionDate() == null || ruleProcessorData.isProjected()) {
			GradMessageRequest gradMessageRequest = GradMessageRequest.builder()
					.gradProgram(existingDataSupport.getGradProgam()).gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding()).programName(ruleProcessorData.getGradProgram().getProgramName()).projected(ruleProcessorData.isProjected())
					.build();
			if(graduationData.isGraduated()) {
				gradMessageRequest.setMsgType("GRADUATED");
			}else {
				gradMessageRequest.setMsgType("NOT_GRADUATED");
			}
			graduationData.setGradMessage(getGradMessages(gradMessageRequest,accessToken,exception,mapOption,ruleProcessorData));
		}

		if(checkSCCPNOPROG) {
			GradMessageRequest gradMessageRequest = GradMessageRequest.builder()
					.gradProgram(existingDataSupport.getGradProgam()).msgType("GRADUATED").gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding()).programName(ruleProcessorData.getGradProgram().getProgramName()).projected(ruleProcessorData.isProjected())
					.build();
			graduationData.setGradMessage(getGradMessages(gradMessageRequest,accessToken,exception,null,ruleProcessorData));
		}
		if(existingDataSupport.getExistingGradMessage() != null && existingDataSupport.getExistingProgramCompletionDate() != null && !existingDataSupport.getGradProgam().equalsIgnoreCase(SCCP) && !existingDataSupport.getGradProgam().equalsIgnoreCase(NOPROGRAM)) {
			graduationData.setGradMessage(existingDataSupport.getExistingGradMessage());
		}
	}

	private void createCompleteGradMessage(StringBuilder currentGradMessage, TranscriptMessage result, Map<String,OptionalProgramRuleProcessor> mapOptional, RuleProcessorData ruleProcessorData) {

		List<String> programs = new ArrayList<>();
		List<String> optPrograms = new ArrayList<>();
		String cpCommaSeparated = null;

		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOptional.entrySet()) {
			String optionalProgramCode = entry.getKey();
			OptionalProgramRuleProcessor obj = entry.getValue();
			if(optionalProgramCode.compareTo("AD")==0 || optionalProgramCode.compareTo("BD")==0 || optionalProgramCode.compareTo("BC")==0) {
				programs.add(obj.getOptionalProgramName());
			}else if(optionalProgramCode.compareTo("CP")==0) {
				cpCommaSeparated = getCareerProgramNames(ruleProcessorData);
			}else {
				optPrograms.add(obj.getOptionalProgramName());
			}
		}
		if(!programs.isEmpty()) {
			currentGradMessage.append(String.format(result.getAdIBProgramMessage(),String.join(",", programs)));
			currentGradMessage.append(" ");
		}
		if(StringUtils.isNotBlank(cpCommaSeparated)) {
			currentGradMessage.append(String.format(result.getCareerProgramMessage(),cpCommaSeparated));
			currentGradMessage.append(" ");
		}
		if(!optPrograms.isEmpty()) {
			currentGradMessage.append(String.format(result.getProgramCadre(),String.join(",", optPrograms)));
			currentGradMessage.append(" ");
		}

	}

	private String getCareerProgramNames(RuleProcessorData ruleProcessorData) {
		List<StudentCareerProgram> cpList = ruleProcessorData.getCpList();
		if(cpList != null) {
			return cpList.stream().map(cp -> String.valueOf(cp.getCareerProgramName())).collect(Collectors.joining(","));
		}
		return null;
	}
}
