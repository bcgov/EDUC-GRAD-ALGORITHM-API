package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

	private static final String NON_GRADUATED = "fromNonGrad";
	private static final String GRADUATED = "fromGraduated";

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
	ParallelDataFetch parallelDataFetch;
    
    @Autowired
    StudentGraduationService studentGraduationService;

    boolean isGraduated = true;
	private static final String SCCP = "SCCP";
	private static final String NOPROGRAM = "NOPROG";

    public GraduationData graduateStudent(UUID studentID, String gradProgram, boolean projected, String accessToken) {
        logger.info("\n************* New Graduation Algorithm START  ************ ");
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
		String schoolOfRecord = ruleProcessorData.getGradStudent().getSchoolOfRecord();
        logger.info("**** PEN: **** {}",pen != null ? pen.substring(5):"Not Found");
        logger.info("**** Grad Program: {}",gradProgram);
		Mono<AlgorithmDataParallelDTO> parallelyCollectedData = parallelDataFetch.fetchAlgorithmRequiredData(gradProgram,pen,schoolOfRecord,accessToken,exception);
		AlgorithmDataParallelDTO algorithmDataParallelDTO = parallelyCollectedData.block();
		//Get All Assessment Requirements, assessments, student assessments
		if(algorithmDataParallelDTO != null) {
			setCourseAssessmentDataForAlgorithm(algorithmDataParallelDTO.courseAlgorithmData(), algorithmDataParallelDTO.assessmentAlgorithmData(), ruleProcessorData);
			//Get All Letter Grades,Optional Case and AlgorithmRules
			setAlgorithmSupportData(algorithmDataParallelDTO.studentGraduationAlgorithmData(), ruleProcessorData);
			//Set Projected flag
			ruleProcessorData.setProjected(projected);

			//Set School of Record for Student
			ruleProcessorData.setSchool(algorithmDataParallelDTO.schoolData());
		}

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
			logger.debug("JSON processing Error {}",e.getMessage());
		}
        gradStatus.setStudentGradData(null);
		boolean checkSCCPNOPROG = existingProgramCompletionDate != null && (gradProgram.equalsIgnoreCase(SCCP) || gradProgram.equalsIgnoreCase(NOPROGRAM));
		if(isGraduated) {
			setGradStatusAlgorithmResponse(gradProgram,existingProgramCompletionDate,gradStatus,checkSCCPNOPROG,ruleProcessorData,accessToken,exception);
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
		gradStudentOptionalAlg.setOptionalProgramCode(optionalProgramCode);
		if(optionalProgramCode.equalsIgnoreCase("CP")) {
			gradStudentOptionalAlg.setCpList(ruleProcessorData.getCpList());
		}

		List<GradRequirement> existingNonGradReasons = null;
		try {
			if(obj.getStudentOptionalProgramData() != null) {
				GradAlgorithmOptionalStudentProgram existingData = new ObjectMapper().readValue(obj.getStudentOptionalProgramData(), GradAlgorithmOptionalStudentProgram.class);
				existingNonGradReasons = existingData.getOptionalNonGradReasons();
			}
		} catch (JsonProcessingException e) {
			logger.debug("JSON processing Error {}",e.getMessage());
		}

		if(obj.getOptionalProgramRules().isEmpty()){
			gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses(new ArrayList<>()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments(new ArrayList<>()));
		}else {
			gradStudentOptionalAlg.setOptionalStudentCourses(obj.getStudentCoursesOptionalProgram() != null ? new StudentCourses(obj.getStudentCoursesOptionalProgram()) : new StudentCourses(new ArrayList<>()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(obj.getStudentAssessmentsOptionalProgram() != null ? new StudentAssessments(obj.getStudentAssessmentsOptionalProgram()) : new StudentAssessments(new ArrayList<>()));
		}
		processExistingNonGradReasonOptionalProgram(existingNonGradReasons,ruleProcessorData,obj);
		if(obj.getRequirementsMetOptionalProgram() != null) {
			reqMet = obj.getRequirementsMetOptionalProgram();
			reqMet.sort(Comparator.comparing(GradRequirement::getRule));
		}
		if(obj.getNonGradReasonsOptionalProgram() != null) {
			nonGradReasons = obj.getNonGradReasonsOptionalProgram();
			nonGradReasons.sort(Comparator.comparing(GradRequirement::getRule));
		}

		if (gradStudentOptionalAlg.isOptionalGraduated() && isGraduated) {
			String mainProgramCompletionDate;
			if (ruleProcessorData.getGradStatus().getProgramCompletionDate().length() > 7) {
				mainProgramCompletionDate = ruleProcessorData.getGradStatus().getProgramCompletionDate();
			}else {
				mainProgramCompletionDate = APIUtils.parsingTraxDate(ruleProcessorData.getGradStatus().getProgramCompletionDate());
			}
			if (!gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList().isEmpty()) {
				String optionalPrgComlDate = getGradDate(gradStudentOptionalAlg.getOptionalStudentCourses().getStudentCourseList());
				gradStudentOptionalAlg.setOptionalProgramCompletionDate(optionalPrgComlDate == null ? mainProgramCompletionDate:optionalPrgComlDate);
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
				opRulePro.setStudentOptionalProgramData(sp.getStudentOptionalProgramData());
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
		strBuilder.append(" ");
		if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
			createCompleteGradMessage(strBuilder,result,mapOptional,ruleProcessorData,NON_GRADUATED);
		}
	}
	private void processMessageForGraduatedStudent(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result, Map<String, OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {
		if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
			if(gradMessageRequest.getHonours().equalsIgnoreCase("Y")) {
				getHonoursMessageForProjected(gradMessageRequest,strBuilder,result);
			}else {
				getMessageForProjected(gradMessageRequest,strBuilder,result);
			}
			if(!gradMessageRequest.isProjected()) {
				strBuilder.append(" ").append(String.format(result.getGradDateMessage(), formatGradDate(gradMessageRequest.getGradDate())));
				strBuilder.append(". ").append(String.format(result.getGraduationSchool(),gradMessageRequest.getSchoolAtGradName()));
			}
			strBuilder.append(". ");
			createCompleteGradMessage(strBuilder,result,mapOptional,ruleProcessorData,GRADUATED);
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
		if(dateFormat.format(gradDate).compareTo("1700-01-01") == 0) {
			return null;
		}
        return dateFormat.format(gradDate);
    }

    private String getGPA(List<StudentCourse> studentCourseList,List<LetterGrade> letterGradesList) {

        studentCourseList = studentCourseList.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
        float totalCredits = studentCourseList.stream().filter(sc-> sc.isUsed()
				&& !sc.getCompletedCourseLetterGrade().equalsIgnoreCase("RM")
				&& !sc.getCompletedCourseLetterGrade().equalsIgnoreCase("SG")
				&& !sc.getCompletedCourseLetterGrade().equalsIgnoreCase("TS")).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
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

	private void setCourseAssessmentDataForAlgorithm(CourseAlgorithmData courseAlgData,AssessmentAlgorithmData assessmentAlgData, RuleProcessorData ruleProcessorData) {
		CourseAlgorithmData courseAlgorithmData = gradCourseService.prepareCourseDataForAlgorithm(courseAlgData);
		if(courseAlgorithmData != null) {
			ruleProcessorData.setStudentCourses(courseAlgorithmData.getStudentCourses());
			ruleProcessorData.setCourseRestrictions(courseAlgorithmData.getCourseRestrictions() != null ? courseAlgorithmData.getCourseRestrictions():null);
			ruleProcessorData.setCourseRequirements(courseAlgorithmData.getCourseRequirements() != null ? courseAlgorithmData.getCourseRequirements():null);
		}

		AssessmentAlgorithmData assessmentAlgorithmData = gradAssessmentService.prepareAssessmentDataForAlgorithm(assessmentAlgData);
		if(assessmentAlgorithmData != null) {
			ruleProcessorData.setStudentAssessments(assessmentAlgorithmData.getStudentAssessments());
			ruleProcessorData.setAssessmentRequirements(assessmentAlgorithmData.getAssessmentRequirements() != null ? assessmentAlgorithmData.getAssessmentRequirements():null);
			ruleProcessorData.setAssessmentList(assessmentAlgorithmData.getAssessments() != null ? assessmentAlgorithmData.getAssessments():null);
		}
		sortCoursesBasedOnProgram(ruleProcessorData.getGradStatus().getProgram(),ruleProcessorData.getStudentCourses() != null?ruleProcessorData.getStudentCourses():new ArrayList<>(),ruleProcessorData.getStudentAssessments() != null? ruleProcessorData.getStudentAssessments():new ArrayList<>());
	}

	private void sortCoursesBasedOnProgram(String program, List<StudentCourse> studentCourses, List<StudentAssessment> studentAssessments) {
		switch (program) {
			case "2018-EN":
				studentCourses.sort(new StudentCoursesComparator(program));
				studentAssessments.sort(Comparator.comparing(StudentAssessment::getProficiencyScore,Comparator.nullsLast(Double::compareTo)).thenComparing(StudentAssessment::getSpecialCase,Comparator.nullsLast(String::compareTo)).thenComparing(StudentAssessment::getSessionDate));
				break;
			case "2018-PF":
			case "2004-EN":
			case "2004-PF":
				studentCourses.sort(new StudentCoursesComparator(program));
				break;
			case "1950":
				studentCourses.sort(Comparator.comparing(StudentCourse::getCourseLevel).thenComparing(StudentCourse::getCompletedCourseLetterGrade,Comparator.nullsLast(String::compareTo)));
				break;
			case "1996-EN":
			case "1996-PF":
			case "1986-EN":
				studentCourses.sort(Comparator.comparing(StudentCourse::getCourseLevel));
				break;
			default:
		}
	}

	private void setAlgorithmSupportData(StudentGraduationAlgorithmData studentGraduationAlgorithmData, RuleProcessorData ruleProcessorData) {
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
			if(data != null) {
				ruleProcessorData.setGradProgramRules(data.getProgramRules());
				v.setOptionalProgramRules(data.getOptionalProgramRules());
				ruleProcessorData.setGradProgram(data.getGradProgram());
			}
		});
		if(!studentHasOp) {
			GradProgramAlgorithmData data = gradProgramService.getProgramDataForAlgorithm(gradProgram, "", accessToken,exception);
			if(data != null) {
				ruleProcessorData.setGradProgramRules(data.getProgramRules());
				ruleProcessorData.setGradProgram(data.getGradProgram());
			}
		}
	}

	private void setGradStatusAlgorithmResponse(String gradProgram, String existingProgramCompletionDate, GradAlgorithmGraduationStudentRecord gradStatus, boolean checkSCCPNOPROG, RuleProcessorData ruleProcessorData,String accessToken,ExceptionMessage exception) {
		if (!gradProgram.equalsIgnoreCase(SCCP) && !gradProgram.equalsIgnoreCase(NOPROGRAM)) {
			//This is done for Reports only grad run -Student already graduated no change in graduation date
			if(existingProgramCompletionDate == null || ruleProcessorData.isProjected()) {
				gradStatus.setProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses()));
			}
			gradStatus.setGpa(getGPA(ruleProcessorData.getStudentCourses(),ruleProcessorData.getLetterGradeList()));
			Pair<Boolean,String> honoursCheckExemption = checkHonoursExemptRule(gradProgram,ruleProcessorData.getStudentCourses());
			boolean isExempted = honoursCheckExemption.getLeft();
			String honoursValue =honoursCheckExemption.getRight();
			if(!isExempted) {
				gradStatus.setHonoursStanding(getHonoursFlag(gradStatus.getGpa()));
			}else{
				gradStatus.setHonoursStanding(honoursValue);
			}
		}
		if(gradStatus.getSchoolAtGrad() != null) {
			School sch = gradSchoolService.getSchoolGrad(gradStatus.getSchoolAtGrad(),accessToken,exception);
			if(sch != null) {
				gradStatus.setSchoolAtGradName(sch.getSchoolName());
			}
		}

		//This is done for Reports only grad run -Student already graduated no change in graduation date
		if((existingProgramCompletionDate == null || ruleProcessorData.isProjected()) && gradStatus.getSchoolAtGrad() == null) {
			gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
			gradStatus.setSchoolAtGradName(ruleProcessorData.getSchool().getSchoolName());
		}

		if(checkSCCPNOPROG) {
			gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
		}
	}

	private Pair<Boolean, String> checkHonoursExemptRule(String program,List<StudentCourse> studentCourseList) {
		boolean isExempted = false;
		String honourValue = null;
		float totalCreditsTSSGRM = studentCourseList.stream().filter(sc-> sc.isUsed()
				&& (sc.getCompletedCourseLetterGrade().equalsIgnoreCase("RM")
				|| sc.getCompletedCourseLetterGrade().equalsIgnoreCase("SG")
				|| sc.getCompletedCourseLetterGrade().equalsIgnoreCase("TS"))).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
		float totalCoursesTSSG = studentCourseList.stream().filter(sc-> sc.isUsed()
				&& (sc.getCompletedCourseLetterGrade().equalsIgnoreCase("SG")
				|| sc.getCompletedCourseLetterGrade().equalsIgnoreCase("TS"))).count();

		switch (program) {
			case "2018-EN":
			case "2018-PF":
				StudentCourse sCCheck = studentCourseList.stream().filter(sc -> sc.isUsed() && (sc.getCourseCode().equalsIgnoreCase("GT")
						|| sc.getCourseCode().equalsIgnoreCase("GTF")
						|| sc.getCourseCode().equalsIgnoreCase("PORT")
						|| sc.getCourseCode().equalsIgnoreCase("PORTF")))
						.findAny()
						.orElse(null);
				if(sCCheck == null) {
					if(totalCreditsTSSGRM > 32) {
						isExempted=true;
						honourValue="N";
					}
				}else {
					if(totalCreditsTSSGRM > 36) {
						isExempted=true;
						honourValue="N";
					}
				}
				break;
			case "1950":
				if(totalCoursesTSSG > 3) {
					isExempted = true;
					honourValue = "N";
				}
				break;
			case "2004-EN":
			case "2004-PF":
				if(totalCreditsTSSGRM > 36) {
					isExempted=true;
					honourValue="N";
				}
				break;
			case "1996-EN":
			case "1996-PF":
				if(totalCreditsTSSGRM > 24) {
					isExempted=true;
					honourValue="N";
				}
				break;
			case "1986-EN":
				if(totalCoursesTSSG > 6) {
					isExempted = true;
					honourValue = "N";
				}
				break;
			default:
		}
		return Pair.of(isExempted,honourValue);
	}

	private void convertRuleProcessorToGraduationData(List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, String existingProgramCompletionDate, List<GradRequirement> existingNonGradReasons, String gradProgram, RuleProcessorData ruleProcessorData,GraduationData graduationData) {
		graduationData.setGradStudent(ruleProcessorData.getGradStudent());
		graduationData.setGradStatus(ruleProcessorData.getGradStatus());
		graduationData.setGradProgram(ruleProcessorData.getGradProgram());
		graduationData.setOptionalGradStatus(optionalProgramStatusList);
		graduationData.setSchool(ruleProcessorData.getSchool());
		graduationData.setStudentCourses(new StudentCourses(ruleProcessorData.getStudentCourses()));
		graduationData.setStudentAssessments(new StudentAssessments(ruleProcessorData.getStudentAssessments()));

		if(ruleProcessorData.getNonGradReasons() != null)
			ruleProcessorData.getNonGradReasons().sort(Comparator.comparing(GradRequirement::getRule, Comparator.nullsLast(String::compareTo)));

		//This is done for Reports only grad run
		if(existingProgramCompletionDate == null || ruleProcessorData.isProjected() || gradProgram.equalsIgnoreCase(SCCP) || gradProgram.equalsIgnoreCase(NOPROGRAM)) {
			graduationData.setNonGradReasons(ruleProcessorData.getNonGradReasons());
		}
		processExistingNonGradReason(existingNonGradReasons,ruleProcessorData,graduationData);
		if(ruleProcessorData.getRequirementsMet() != null)
			ruleProcessorData.getRequirementsMet().sort(Comparator.comparing(GradRequirement::getRule, Comparator.nullsLast(String::compareTo)));

		graduationData.setRequirementsMet(ruleProcessorData.getRequirementsMet());
		graduationData.setGraduated(ruleProcessorData.isGraduated());
		if(graduationData.getGradStatus().getProgramCompletionDate() == null && gradProgram.equalsIgnoreCase(SCCP)) {
			graduationData.setGraduated(false);
		}
	}

	private void processExistingNonGradReasonOptionalProgram(List<GradRequirement> existingNonGradReasons, RuleProcessorData ruleProcessorData, OptionalProgramRuleProcessor obj) {
		if(existingNonGradReasons != null && !existingNonGradReasons.isEmpty() && ruleProcessorData.isProjected()) {
			for (GradRequirement gR : existingNonGradReasons) {
				boolean ruleExists = false;
				if (obj.getNonGradReasonsOptionalProgram() != null) {
					ruleExists = obj.getNonGradReasonsOptionalProgram().stream().anyMatch(nGR -> nGR.getRule().compareTo(gR.getRule()) == 0);
				}
				if (!ruleExists && ruleProcessorData.getRequirementsMet() != null) {
					obj.getRequirementsMetOptionalProgram().stream().filter(rM -> rM.getRule().compareTo(gR.getRule()) == 0).forEach(rM -> rM.setProjected(true));
				}
			}
		}
	}

	private void processExistingNonGradReason(List<GradRequirement> existingNonGradReasons,RuleProcessorData ruleProcessorData,GraduationData graduationData) {
		if(existingNonGradReasons != null && !existingNonGradReasons.isEmpty() && ruleProcessorData.isProjected()) {
			for (GradRequirement gR : existingNonGradReasons) {
				boolean ruleExists = false;
				if (graduationData.getNonGradReasons() != null) {
					ruleExists = graduationData.getNonGradReasons().stream().anyMatch(nGR -> nGR.getRule() != null && nGR.getRule().compareTo(gR.getRule()) == 0);
				}
				if (!ruleExists && ruleProcessorData.getRequirementsMet() != null) {
					ruleProcessorData.getRequirementsMet().stream().filter(rM -> rM.getRule() != null && rM.getRule().compareTo(gR.getRule()) == 0).forEach(rM -> rM.setProjected(true));
				}
			}
		}
	}

	private void processGradMessages(boolean checkSCCPNOPROG, ExistingDataSupport existingDataSupport, String accessToken, ExceptionMessage exception, Map<String, OptionalProgramRuleProcessor> mapOption,RuleProcessorData ruleProcessorData,GraduationData graduationData) {
		if(existingDataSupport.getExistingProgramCompletionDate() == null || ruleProcessorData.isProjected()) {
			GradMessageRequest gradMessageRequest = GradMessageRequest.builder()
					.gradProgram(existingDataSupport.getGradProgam()).gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding()).programName(ruleProcessorData.getGradProgram().getProgramName()).projected(ruleProcessorData.isProjected())
					.schoolAtGradName(graduationData.getGradStatus().getSchoolAtGradName())
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
					.gradProgram(existingDataSupport.getGradProgam()).msgType(graduationData.isGraduated()?"GRADUATED":"NOT_GRADUATED").gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding()).programName(ruleProcessorData.getGradProgram().getProgramName()).projected(ruleProcessorData.isProjected())
					.build();
			graduationData.setGradMessage(getGradMessages(gradMessageRequest,accessToken,exception,null,ruleProcessorData));
		}
		if(existingDataSupport.getExistingGradMessage() != null && existingDataSupport.getExistingProgramCompletionDate() != null && !existingDataSupport.getGradProgam().equalsIgnoreCase(SCCP) && !existingDataSupport.getGradProgam().equalsIgnoreCase(NOPROGRAM)) {
			graduationData.setGradMessage(existingDataSupport.getExistingGradMessage());
		}
	}

	private void createCompleteGradMessage(StringBuilder currentGradMessage, TranscriptMessage result, Map<String,OptionalProgramRuleProcessor> mapOptional, RuleProcessorData ruleProcessorData,String opMessage) {

		List<String> programs = new ArrayList<>();
		List<String> optPrograms = new ArrayList<>();
		String cpCommaSeparated = null;
		boolean dualDogwoodGraduated = false;
		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOptional.entrySet()) {
			String optionalProgramCode = entry.getKey();
			OptionalProgramRuleProcessor obj = entry.getValue();

			if(opMessage.equalsIgnoreCase(NON_GRADUATED) || (obj.isOptionalProgramGraduated() && opMessage.equalsIgnoreCase(GRADUATED))) {
				if (optionalProgramCode.compareTo("AD") == 0 || optionalProgramCode.compareTo("BD") == 0 || optionalProgramCode.compareTo("BC") == 0) {
					programs.add(obj.getOptionalProgramName());
				}
				if (optionalProgramCode.compareTo("CP") == 0) {
					cpCommaSeparated = getCareerProgramNames(ruleProcessorData);
				}
				if (optionalProgramCode.compareTo("DD") != 0) {
					optPrograms.add(obj.getOptionalProgramName());
				}
				if(optionalProgramCode.compareTo("DD") == 0) {
					dualDogwoodGraduated = true;
				}
			}
		}
		if(StringUtils.isNotBlank(cpCommaSeparated)) {
			currentGradMessage.append(String.format(result.getCareerProgramMessage(),cpCommaSeparated));
			currentGradMessage.append(". ");
		}
		if(!programs.isEmpty()) {
			currentGradMessage.append(String.format(result.getAdIBProgramMessage(),String.join(",", programs)));
			currentGradMessage.append(". ");
		}
		if(!optPrograms.isEmpty()) {
			currentGradMessage.append(String.format(result.getProgramCadre(),String.join(",", optPrograms)));
			currentGradMessage.append(". ");
		}

		if(ruleProcessorData.getGradProgram().getProgramCode().contains("-PF") && dualDogwoodGraduated && opMessage.equalsIgnoreCase(GRADUATED)) {
			currentGradMessage.append("Student has successfully completed the Programme Francophone");
			currentGradMessage.append(". ");
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
