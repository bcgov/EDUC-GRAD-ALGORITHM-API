package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmApiUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.xml.transform.TransformerException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.DEFAULT_DATE_FORMAT;
import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.SECONDARY_DATE_FORMAT;

@Slf4j
@Service
public class GradAlgorithmService {

	private static final String NON_GRADUATED = "fromNonGrad";
	private static final String GRADUATED = "fromGraduated";
	public static final String MSG_TYPE_GRADUATED = "GRADUATED";
	public static final String MSG_TYPE_NOT_GRADUATED = "NOT_GRADUATED";

	GradStudentService gradStudentService;
    GradAssessmentService gradAssessmentService;
    GradCourseService gradCourseService;
	GradProgramService gradProgramService;
    GradGraduationStatusService gradGraduationStatusService;
    GradRuleProcessorService gradRuleProcessorService;
	GradSchoolService gradSchoolService;
	ParallelDataFetch parallelDataFetch;
	ca.bc.gov.educ.api.gradalgorithm.service.v2.ParallelDataFetch parallelDataFetchV2;
	StudentGraduationService studentGraduationService;
	JsonTransformer jsonTransformer;

	@Autowired
	public GradAlgorithmService(GradStudentService gradStudentService, GradAssessmentService gradAssessmentService,
								GradCourseService gradCourseService, GradProgramService gradProgramService,
								GradGraduationStatusService gradGraduationStatusService, GradRuleProcessorService gradRuleProcessorService,
								GradSchoolService gradSchoolService, ParallelDataFetch parallelDataFetch,
								StudentGraduationService studentGraduationService, JsonTransformer jsonTransformer,
								ca.bc.gov.educ.api.gradalgorithm.service.v2.ParallelDataFetch parallelDataFetchV2) {
		this.gradStudentService = gradStudentService;
		this.gradAssessmentService = gradAssessmentService;
		this.gradCourseService = gradCourseService;
		this.gradProgramService = gradProgramService;
		this.gradGraduationStatusService = gradGraduationStatusService;
		this.gradRuleProcessorService = gradRuleProcessorService;
		this.gradSchoolService = gradSchoolService;
		this.parallelDataFetch = parallelDataFetch;
		this.studentGraduationService = studentGraduationService;
		this.jsonTransformer = jsonTransformer;
		this.parallelDataFetchV2 = parallelDataFetchV2;
	}

	private static final String SCCP = "SCCP";
	private static final String NOPROGRAM = "NOPROG";

    public GraduationData graduateStudent(UUID studentID, String gradProgram, boolean projected, String hypotheticalGradYear, boolean fetchDataUsingV2) {
        log.debug("\n************* New Graduation Algorithm START  ************ ");
        //Get Student Demographics
		RuleProcessorData ruleProcessorData = new RuleProcessorData();
		GraduationData graduationData = new GraduationData();
		Map<String, OptionalProgramRuleProcessor> mapOptional = new HashMap<>();
		ruleProcessorData.setMapOptional(mapOptional);
        ExceptionMessage exception = new ExceptionMessage();
		GradStudentAlgorithmData gradStudentAlgorithmData = gradStudentService.getGradStudentData(studentID, exception);
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
		String schoolOfRecordId = ruleProcessorData.getGradStudent().getSchoolOfRecordId();
		log.info("**** PEN: **** {}",pen != null ? pen.substring(5):"Not Found");
			Mono<AlgorithmDataParallelDTO> parallelCollectedData;
		if(fetchDataUsingV2) {
			parallelCollectedData = parallelDataFetchV2.fetchAlgorithmRequiredData(studentID, exception);
		} else {
			parallelCollectedData =parallelDataFetch.fetchAlgorithmRequiredData(pen, exception);
		}
		AlgorithmDataParallelDTO algorithmDataParallelDTO = parallelCollectedData.block();
		//Get All Assessment Requirements, assessments, student assessments
		if(algorithmDataParallelDTO != null) {
			setCourseAssessmentDataForAlgorithm(
					algorithmDataParallelDTO.courseAlgorithmData(),
					algorithmDataParallelDTO.assessmentAlgorithmData(),
					ruleProcessorData,
					hypotheticalGradYear);
		}
		//Set Projected flag
		ruleProcessorData.setProjected(projected);
		//Set School of Record for Student
		ruleProcessorData.setSchool(gradSchoolService.retrieveSchoolBySchoolId(schoolOfRecordId));
		//Get All Letter Grades,Optional Case and AlgorithmRules
		setAlgorithmSupportData(studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradProgram),ruleProcessorData);
        //Set Optional Program Flag
		checkForOptionalProgram(ruleProcessorData.getGradStudent().getStudentID(), ruleProcessorData, exception);
		manageProgramRules(gradProgram,ruleProcessorData);

		//Calling Rule Processor
        ruleProcessorData = gradRuleProcessorService.processGradAlgorithmRules(ruleProcessorData, exception);
        if(exception.getExceptionName() != null) {
			graduationData.setException(exception);
			return graduationData;
        }

        //Populate Grad Status Details
        String existingProgramCompletionDate = gradStatus.getProgramCompletionDate();
        List<GradRequirement> existingNonGradReasons = null;
        String existingGradMessage = null;
        try {
			if(gradStatus.getStudentGradData() != null) {
				GraduationData existingData = (GraduationData)jsonTransformer.unmarshall(gradStatus.getStudentGradData(), GraduationData.class);
				existingNonGradReasons = existingData.getNonGradReasons();
				existingGradMessage = existingData.getGradMessage();
			}
		} catch (TransformerException e) {
			log.error("JSON processing Error {}",e.getMessage());
		}
		gradStatus.setStudentGradData(null);
		boolean checkSCCPNOPROG = existingProgramCompletionDate != null
				&& (gradProgram.equalsIgnoreCase(SCCP) || gradProgram.equalsIgnoreCase(NOPROGRAM));
		if(ruleProcessorData.isGraduated()) {
			setGradStatusAlgorithmResponse(gradProgram,
					existingProgramCompletionDate, gradStatus,
					checkSCCPNOPROG,ruleProcessorData);
        }
        ruleProcessorData.setGradStatus(gradStatus);

        //Populating Optional Grad Status
		List<GradRequirement> existingOptionalNonGradReasons = new ArrayList<>();
        List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList = new ArrayList<>();
        Map<String,OptionalProgramRuleProcessor> mapOption = ruleProcessorData.getMapOptional();
		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOption.entrySet()) {
			String optionalProgramCode = entry.getKey();
			OptionalProgramRuleProcessor obj = entry.getValue();
			getListOfOptionalProgramStatus(optionalProgramCode, obj,
					optionalProgramStatusList, ruleProcessorData,
					existingOptionalNonGradReasons);
		}

        //Convert ruleProcessorData into GraduationData object
		convertRuleProcessorToGraduationData(optionalProgramStatusList,existingProgramCompletionDate,existingNonGradReasons,gradProgram,ruleProcessorData,graduationData);
        //This is done for Reports only grad run - Student already graduated, no change in grad message
        ExistingDataSupport existingDataSupport = ExistingDataSupport.builder()
				.existingProgramCompletionDate(existingProgramCompletionDate)
				.existingGradMessage(existingGradMessage)
				.gradProgram(gradProgram)
				.optionalNonGradReasons(existingOptionalNonGradReasons)
				.build();
		processGradMessages(checkSCCPNOPROG, existingDataSupport, mapOption, ruleProcessorData, graduationData);

        if(exception.getExceptionName() != null) {
			graduationData.setException(exception);
			return graduationData;
        }
		log.debug("\n************* Graduation Algorithm END  ************");

        return graduationData;
    }

	/******************************************************************************************************************
	 Utility Methods
	 *******************************************************************************************************************/
	private void getListOfOptionalProgramStatus(String optionalProgramCode,OptionalProgramRuleProcessor obj,
												List<GradAlgorithmOptionalStudentProgram> optionalProgramStatusList, RuleProcessorData ruleProcessorData,
												List<GradRequirement> existingOptionalNonGradReasons) {
		List<GradRequirement> nonGradReasons = new ArrayList<>();
		List<GradRequirement> reqMet = new ArrayList<>();
		GradAlgorithmOptionalStudentProgram gradStudentOptionalAlg = new GradAlgorithmOptionalStudentProgram();
		gradStudentOptionalAlg.setOptionalProgramID(obj.getOptionalProgramID());
		gradStudentOptionalAlg.setStudentID(UUID.fromString(ruleProcessorData.getGradStudent().getStudentID()));
		gradStudentOptionalAlg.setOptionalGraduated(obj.isOptionalProgramGraduated());
		gradStudentOptionalAlg.setOptionalProgramCode(optionalProgramCode);
		if(optionalProgramCode.equalsIgnoreCase("CP")) {
			gradStudentOptionalAlg.setCpList(ruleProcessorData.getCpList());
		}

		try {
			if(obj.getStudentOptionalProgramData() != null) {
				GradAlgorithmOptionalStudentProgram existingData = (GradAlgorithmOptionalStudentProgram)jsonTransformer.unmarshall(obj.getStudentOptionalProgramData(), GradAlgorithmOptionalStudentProgram.class);
				if (existingData != null && existingData.getOptionalNonGradReasons() != null && !existingData.getOptionalNonGradReasons().isEmpty()) {
					existingOptionalNonGradReasons.addAll(existingData.getOptionalNonGradReasons());
				}
			}
		} catch (TransformerException e) {
			log.debug("JSON processing Error {}",e.getMessage());
		}

		if(obj.getOptionalProgramRules().isEmpty()){
			gradStudentOptionalAlg.setOptionalStudentCourses(new StudentCourses(new ArrayList<>()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(new StudentAssessments(new ArrayList<>()));
		}else {
			gradStudentOptionalAlg.setOptionalStudentCourses(obj.getStudentCoursesOptionalProgram() != null ? new StudentCourses(obj.getStudentCoursesOptionalProgram()) : new StudentCourses(new ArrayList<>()));
			gradStudentOptionalAlg.setOptionalStudentAssessments(obj.getStudentAssessmentsOptionalProgram() != null ? new StudentAssessments(obj.getStudentAssessmentsOptionalProgram()) : new StudentAssessments(new ArrayList<>()));
		}
		processExistingNonGradReasonOptionalProgram(existingOptionalNonGradReasons,ruleProcessorData,obj);
		if(obj.getRequirementsMetOptionalProgram() != null) {
			reqMet = obj.getRequirementsMetOptionalProgram();
			reqMet.sort(Comparator.comparing(GradRequirement::getRule));
		}
		if(obj.getNonGradReasonsOptionalProgram() != null) {
			nonGradReasons = obj.getNonGradReasonsOptionalProgram();
			nonGradReasons.sort(Comparator.comparing(GradRequirement::getRule));
		}
		processGraduation(gradStudentOptionalAlg,ruleProcessorData);
		gradStudentOptionalAlg.setOptionalNonGradReasons(nonGradReasons);
		gradStudentOptionalAlg.setOptionalRequirementsMet(reqMet);
		optionalProgramStatusList.add(gradStudentOptionalAlg);
	}

	private  void processGraduation(GradAlgorithmOptionalStudentProgram gradStudentOptionalAlg, RuleProcessorData ruleProcessorData) {

		if (gradStudentOptionalAlg.isOptionalGraduated() && ruleProcessorData.isGraduated()) {
			String result = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
            gradStudentOptionalAlg.setOptionalProgramCompletionDate(result);
		}
	}

	private void checkForOptionalProgram(String studentID, RuleProcessorData ruleProcessorData, ExceptionMessage exception) {
		List<StudentOptionalProgram> gradOptionalResponseList = gradGraduationStatusService.getStudentOptionalProgramsById(studentID, exception);
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

    private String getGradMessages(GradMessageRequest gradMessageRequest,Map<String,OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {
		StringBuilder strBuilder = new StringBuilder();
		StudentGraduationAlgorithmData data = studentGraduationService.retrieveStudentGraduationDataByProgramCode(gradMessageRequest.getGradProgram());
		if(StringUtils.equalsIgnoreCase(gradMessageRequest.getMsgType(), MSG_TYPE_GRADUATED)) {
			processMessageForGraduatedStudent(gradMessageRequest, strBuilder,
					data.getGraduatedMessage(), mapOptional, ruleProcessorData);
		} else {
			processMessageForUnGraduatedStudent(gradMessageRequest, strBuilder,
					data.getNonGraduateMessage(), mapOptional, ruleProcessorData);
		}
		return strBuilder.toString();
	}

	private void processMessageForUnGraduatedStudent(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result, Map<String, OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {
		getMainMessageForNonGrad(gradMessageRequest,strBuilder,result);
		appendPeriod(strBuilder);
		if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
			createCompleteGradMessage(gradMessageRequest, strBuilder, result, mapOptional, ruleProcessorData, NON_GRADUATED);
		}
	}

	private void processMessageForGraduatedStudent(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result, Map<String, OptionalProgramRuleProcessor> mapOptional,RuleProcessorData ruleProcessorData) {
		if(!gradMessageRequest.getGradProgram().equalsIgnoreCase(SCCP)) {
			if("Y".equalsIgnoreCase(gradMessageRequest.getHonours())) {
				getHonoursMainMessage(gradMessageRequest, strBuilder, result);
			} else {
				getMainMessage(gradMessageRequest, strBuilder, result);
			}
			appendPeriod(strBuilder);
			createCompleteGradMessage(gradMessageRequest, strBuilder, result, mapOptional, ruleProcessorData, GRADUATED);
			// graduation date & graduation school
			if(gradMessageRequest.isPullGraduatedMessage()) {
				strBuilder.append("\n\n");
				strBuilder.append(String.format(result.getGradDateMessage(), formatGradDate(gradMessageRequest.getGradDate())));
				if (StringUtils.isNotBlank(gradMessageRequest.getSchoolAtGradName())) {
					appendPeriod(strBuilder);
					strBuilder.append(String.format(result.getGraduationSchool(), gradMessageRequest.getSchoolAtGradName()));
				}
			}
			lastPeriod(strBuilder);
		} else {
			getMainMessage(gradMessageRequest,strBuilder,result);
		}
	}

	private void appendPeriod(StringBuilder strBuilder) {
		if (!strBuilder.isEmpty() && '.' != (strBuilder.charAt(strBuilder.length() - 1 ))) {
			strBuilder.append(". ");
		} else {
			strBuilder.append(" ");
		}
	}

	private void lastPeriod(StringBuilder strBuilder) {
		if (strBuilder.length() > 1) {
			if ('.' == (strBuilder.charAt(strBuilder.length() - 2 )) && ' ' == (strBuilder.charAt(strBuilder.length() - 1 ))) {
				// if '. ' is at the end, do nothing.
			} else if (' ' == (strBuilder.charAt(strBuilder.length() - 1 ))) {
				strBuilder.setCharAt(strBuilder.length() - 1, '.');
			} else if ('.' != (strBuilder.charAt(strBuilder.length() - 1 ))) {
				strBuilder.append(".");
			}
		}
	}

	private void getHonoursMainMessage(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result) {
		if(gradMessageRequest.isPullGraduatedMessage()) {
			// "has graduated"
			strBuilder.append(String.format(result.getHonourNote(), gradMessageRequest.getProgramName()));
		} else {
			// "should be able to graduate"
			strBuilder.append(String.format(result.getHonourProjectedNote(), gradMessageRequest.getProgramName()));
		}
	}

	private void getMainMessage(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result) {
		if (gradMessageRequest.isPullGraduatedMessage()) {
			// "has graduated"
			strBuilder.append(String.format(result.getGradMainMessage(), gradMessageRequest.getProgramName()));
		} else {
			// "should be able to graduate"
			strBuilder.append(String.format(result.getGradProjectedMessage(), gradMessageRequest.getProgramName()));
		}
	}

	private void getMainMessageForNonGrad(GradMessageRequest gradMessageRequest, StringBuilder strBuilder, TranscriptMessage result) {
		if (!gradMessageRequest.isProjected()) {
			// "has not yet graduated"
			strBuilder.append(String.format(result.getGradMainMessage(), gradMessageRequest.getProgramName()));
		} else {
			// "cannot satisfy"
			strBuilder.append(String.format(result.getGradProjectedMessage(), gradMessageRequest.getProgramName()));
		}
	}
    
    private String formatGradDate(String gradDate) {
		try {
			String formatDate = StringUtils.contains(gradDate, "/") ? StringUtils.replace(gradDate + "/01", "/", "-") : gradDate;
			LocalDate currentDate = LocalDate.parse(formatDate).with(TemporalAdjusters.lastDayOfMonth());
			Month month = currentDate.getMonth();
			int year = currentDate.getYear();
			return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
		} catch (Exception e) {
			log.error("Unable to parse date {}", gradDate);
			return gradDate;
		}
    }

    private String getGradDate(List<StudentCourse> studentCourses, List<StudentAssessment> studentAssessments) {

        studentCourses = studentCourses
                .stream()
                .filter(StudentCourse::isUsed)
                .collect(Collectors.toList());

		studentAssessments = studentAssessments
				.stream()
				.filter(StudentAssessment::isUsed)
				.filter(sa -> "E".compareTo(sa.getSpecialCase()) != 0)
				.collect(Collectors.toList());

        return getLastSessionDate(studentCourses, studentAssessments);
    }

	private String getLastSessionDate(List<StudentCourse> studentCourses, List<StudentAssessment> studentAssessments) {

		Date gradDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat(SECONDARY_DATE_FORMAT);

		try {
			gradDate = dateFormat.parse("1700/01/01");
		} catch (ParseException e) {
			log.debug(e.getMessage());
		}

		for (StudentCourse studentCourse : studentCourses) {
			try {
				Date dateToCompare = toLastDayOfMonth(dateFormat.parse(studentCourse.getSessionDate() + "/01"));
				if (dateToCompare.compareTo(gradDate) > 0) {
					gradDate = dateToCompare;
				}
			} catch (ParseException e) {
				log.debug(e.getMessage());
			}
		}

		for (StudentAssessment studentAssessment : studentAssessments) {
			try {
				Date dateToCompare = toLastDayOfMonth(dateFormat.parse(studentAssessment.getSessionDate() + "/01"));
				if (dateToCompare.compareTo(gradDate) > 0) {
					gradDate = dateToCompare;
				}
			} catch (ParseException e) {
				log.debug(e.getMessage());
			}
		}

		String result = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(gradDate);
		if("1700-01-01".compareTo(result) == 0) {
			return null;
		}
		return result;
	}

    private String getGPA(List<StudentCourse> studentCourseList,List<LetterGrade> letterGradesList) {

        studentCourseList = studentCourseList.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
        float totalCredits = studentCourseList.stream().filter(sc-> sc.isUsed()
				&& !"RM".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
				&& !"SG".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
				&& !"TS".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
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
            } else {
				if ("RM".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
						|| "SG".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
						|| "TS".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())) {
					tempGpaMV = "0";
				}
            }
			if (StringUtils.equalsIgnoreCase("P", tempGpaMV)) {
				log.debug("Temporary FinalLG for Hypothetical passed course: {} / {} => credits for used [{}]", sc.getCourseCode(), sc.getCourseLevel(), sc.getCreditsUsedForGrad());
			}
            float gpaMarkValue = Float.parseFloat(tempGpaMV);

            acquiredCredits += (gpaMarkValue * sc.getCreditsUsedForGrad());

			log.debug("[{}/{}] Letter Grade: {} | GPA Mark Value: {} | Credits Used For GRAD: {} | Acquired Credits: {} | Total Credits: {}",
					sc.getCourseCode(), sc.getCourseLevel(), letterGrade,gpaMarkValue,sc.getCreditsUsedForGrad(),acquiredCredits,totalCredits);
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

	private void setCourseAssessmentDataForAlgorithm(CourseAlgorithmData courseAlgData,AssessmentAlgorithmData assessmentAlgData, RuleProcessorData ruleProcessorData, String hypotheticalGradYear) {
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
		if (NumberUtils.isCreatable(hypotheticalGradYear)) {
			setCoursesForHypotheticalPass(ruleProcessorData.getStudentCourses() != null?ruleProcessorData.getStudentCourses():new ArrayList<>(), hypotheticalGradYear);
		}
		sortCoursesBasedOnProgram(ruleProcessorData.getGradStatus().getProgram(),
				ruleProcessorData.getStudentCourses() != null?ruleProcessorData.getStudentCourses():new ArrayList<>(),
				ruleProcessorData.getStudentAssessments() != null? ruleProcessorData.getStudentAssessments():new ArrayList<>(),
				ruleProcessorData.getGradStatus().getAdultStartDate());
	}

	private void setCoursesForHypotheticalPass(List<StudentCourse> studentCourses, String hypotheticalGradYear) {
		DateFormat dateFormat = new SimpleDateFormat(SECONDARY_DATE_FORMAT);
		String basisSession = hypotheticalGradYear + "/09/01";
		Date basisSessionDate = null;
		try {
			basisSessionDate = dateFormat.parse(basisSession);
		} catch (ParseException e) {
			log.debug(e.getMessage());
		}

		if (basisSessionDate != null && !studentCourses.isEmpty()) {
			for (StudentCourse studentCourse : studentCourses) {
				try {
					Date dateToCompare = toLastDayOfMonth(dateFormat.parse(studentCourse.getSessionDate() + "/01"));
					if (dateToCompare.compareTo(basisSessionDate) >= 0) {
						log.debug("Student Course [{}/{}] Session Date [{}] - Hypothetically passed", studentCourse.getCourseCode(), studentCourse.getCourseLevel(), studentCourse.getSessionDate());
						studentCourse.setCompletedCourseLetterGrade("P");
					}
				} catch (ParseException e) {
					log.debug(e.getMessage());
				}
			}
		}
	}

	private void sortCoursesBasedOnProgram(String program, List<StudentCourse> studentCourses,
										   List<StudentAssessment> studentAssessments, Date adultStartDate) {
		switch (program) {
			case "2018-EN", "2023-EN" -> {
				studentCourses.sort(new StudentCoursesComparator(program));
				studentAssessments.sort(
						Comparator.comparing(StudentAssessment::getProficiencyScore, Comparator.nullsLast(Double::compareTo)).reversed()
								.thenComparing(StudentAssessment::getSpecialCase, Comparator.nullsLast(String::compareTo))
								.thenComparing(StudentAssessment::getSessionDate));
			}
			case "2018-PF", "2023-PF", "2004-EN", "2004-PF" -> studentCourses.sort(new StudentCoursesComparator(program));
			case "1950" -> {
				/*
				 * Split Student courses into 2 parts
				 * 1. Courses taken after start date
				 * 2. Courses taken on or before start date
				 * Sort #1 by Final LG, Final % desc
				 * Sort #2 by Final LG, Final % desc
				 * Join 2 lists
				 */
				List<StudentCourse> coursesAfterStartDate = new ArrayList<>();
				List<StudentCourse> coursesOnOrBeforeStartDate = new ArrayList<>();
				for (StudentCourse sc : studentCourses) {
					String courseSessionDate = sc.getSessionDate() + "/01";
					Date temp = null;
					try {
						temp = toLastDayOfMonth(GradAlgorithmApiUtils.parseDate(courseSessionDate, SECONDARY_DATE_FORMAT));
					} catch (ParseException e) {
						log.debug(e.getMessage());
					}

					if (adultStartDate != null && temp != null && temp.compareTo(adultStartDate) > 0) {
						coursesAfterStartDate.add(sc);
					} else {
						coursesOnOrBeforeStartDate.add(sc);
					}
				}
				studentCourses.clear();
				if (!coursesAfterStartDate.isEmpty()) {
					coursesAfterStartDate.sort(
							Comparator.comparing(StudentCourse::getCourseLevel)
									.thenComparing(StudentCourse::getCompletedCourseLetterGrade, Comparator.nullsLast(String::compareTo))
									.thenComparing(StudentCourse::getCompletedCoursePercentage, Comparator.nullsLast(Double::compareTo).reversed())
					);
					studentCourses.addAll(coursesAfterStartDate);
				}
				if (!coursesOnOrBeforeStartDate.isEmpty()) {
					coursesOnOrBeforeStartDate.sort(
							Comparator.comparing(StudentCourse::getCourseLevel)
									.thenComparing(StudentCourse::getCompletedCourseLetterGrade, Comparator.nullsLast(String::compareTo))
									.thenComparing(StudentCourse::getCompletedCoursePercentage, Comparator.nullsLast(Double::compareTo).reversed())
					);
					studentCourses.addAll(coursesOnOrBeforeStartDate);
				}
			}
			case "1996-EN", "1996-PF", "1986-EN", "1986-PF" -> studentCourses.sort(Comparator.comparingInt(sc -> APIUtils.getNumericCourseLevel(sc.getCourseLevel())));
			default -> log.debug("No sorting order specified for program: {}", program);
		}
	}

	private void setAlgorithmSupportData(StudentGraduationAlgorithmData studentGraduationAlgorithmData, RuleProcessorData ruleProcessorData) {
		if(studentGraduationAlgorithmData != null) {
			ruleProcessorData.setLetterGradeList(studentGraduationAlgorithmData.getLetterGrade());
			ruleProcessorData.setSpecialCaseList(studentGraduationAlgorithmData.getSpecialCase());
			ruleProcessorData.setAlgorithmRules(studentGraduationAlgorithmData.getProgramAlgorithmRules());
		}
	}

	private void manageProgramRules(String gradProgram, RuleProcessorData ruleProcessorData) {
		Map<String,OptionalProgramRuleProcessor> mapOpt = ruleProcessorData.getMapOptional();
		boolean studentHasOp = mapOpt.size() > 0;
		mapOpt.forEach((k,v) ->{
			GradProgramAlgorithmData data = gradProgramService.retrieveProgramDataByProgramCode(gradProgram,k);
			if(data != null) {
				ruleProcessorData.setGradProgramRules(data.getProgramRules());
				v.setOptionalProgramRules(data.getOptionalProgramRules());
				v.setOptionalProgramID(data.getOptionalProgramID());
				ruleProcessorData.setGradProgram(data.getGradProgram());
			}
		});
		if(!studentHasOp) {
			GradProgramAlgorithmData data = gradProgramService.retrieveProgramDataByProgramCode(gradProgram, "");
			if(data != null) {
				ruleProcessorData.setGradProgramRules(data.getProgramRules());
				ruleProcessorData.setGradProgram(data.getGradProgram());
			}
		}
	}

	private void setGradStatusAlgorithmResponse(String gradProgram, String existingProgramCompletionDate, GradAlgorithmGraduationStudentRecord gradStatus, boolean checkSCCPNOPROG, RuleProcessorData ruleProcessorData) {
		if (!gradProgram.equalsIgnoreCase(SCCP) && !gradProgram.equalsIgnoreCase(NOPROGRAM)) {
			// This is done for Reports only grad run -Student already graduated no change in graduation date
			if(existingProgramCompletionDate == null) {
				gradStatus.setProgramCompletionDate(getGradDate(ruleProcessorData.getStudentCourses(), ruleProcessorData.getStudentAssessments()));
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
		if(gradStatus.getSchoolAtGradId() != null) {
			School sch = gradSchoolService.retrieveSchoolBySchoolId(gradStatus.getSchoolAtGradId().toString());
			if(sch != null) {
				gradStatus.setSchoolAtGradName(sch.getSchoolName());
			}
		}

		//This is done for Reports only grad run -Student already graduated no change in graduation date
		if((existingProgramCompletionDate == null || ruleProcessorData.isProjected()) && gradStatus.getSchoolAtGradId() == null) {
			gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
			gradStatus.setSchoolAtGradId(ruleProcessorData.getGradStudent().getSchoolOfRecordId() != null?
					UUID.fromString(ruleProcessorData.getGradStudent().getSchoolOfRecordId()) : null);
			gradStatus.setSchoolAtGradName(ruleProcessorData.getSchool().getSchoolName());
		}

		if(checkSCCPNOPROG && gradStatus.getSchoolAtGradId() == null) {
				gradStatus.setSchoolAtGrad(ruleProcessorData.getGradStudent().getSchoolOfRecord());
				gradStatus.setSchoolAtGradId(
						UUID.fromString(ruleProcessorData.getGradStudent().getSchoolOfRecordId()));
		}
	}

	private Pair<Boolean, String> checkHonoursExemptRule(String program,List<StudentCourse> studentCourseList) {
		boolean isExempted = false;
		String honourValue = null;
		float totalCreditsTSSGRM = studentCourseList.stream().filter(sc-> sc.isUsed()
				&& ("RM".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
				|| "SG".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
				|| "TS".equalsIgnoreCase(sc.getCompletedCourseLetterGrade()))).mapToInt(StudentCourse::getCreditsUsedForGrad).sum();
		float totalCoursesTSSG = studentCourseList.stream().filter(sc-> sc.isUsed()
				&& ("SG".equalsIgnoreCase(sc.getCompletedCourseLetterGrade())
				|| "TS".equalsIgnoreCase(sc.getCompletedCourseLetterGrade()))).count();

		switch (program) {
			case "2023-EN", "2023-PF", "2018-EN", "2018-PF" -> {
				StudentCourse sCCheck = studentCourseList.stream().filter(sc -> sc.isUsed() && (sc.getCourseCode().equalsIgnoreCase("GT")
								|| sc.getCourseCode().equalsIgnoreCase("GTF")
								|| sc.getCourseCode().equalsIgnoreCase("PORT")
								|| sc.getCourseCode().equalsIgnoreCase("PORTF")))
						.findAny()
						.orElse(null);
				if (sCCheck == null) {
					if (totalCreditsTSSGRM > 32) {
						isExempted = true;
						honourValue = "N";
					}
				} else {
					if (totalCreditsTSSGRM > 36) {
						isExempted = true;
						honourValue = "N";
					}
				}
			}
			case "1950" -> {
				if (totalCoursesTSSG > 3) {
					isExempted = true;
					honourValue = "N";
				}
			}
			case "2004-EN", "2004-PF" -> {
				if (totalCreditsTSSGRM > 36) {
					isExempted = true;
					honourValue = "N";
				}
			}
			case "1996-EN", "1996-PF" -> {
				if (totalCreditsTSSGRM > 24) {
					isExempted = true;
					honourValue = "N";
				}
			}
			case "1986-EN", "1986-PF" -> {
				if (totalCoursesTSSG > 6) {
					isExempted = true;
					honourValue = "N";
				}
			}
			default -> log.error("Unexpected program: {}",  program);
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
		graduationData.setLatestSessionDate(getLastSessionDate(ruleProcessorData.getStudentCourses(), ruleProcessorData.getStudentAssessments()));

		if(ruleProcessorData.getNonGradReasons() != null) {

			//Remove Duplicate NonGradReasons
			Set<GradRequirement> s = new HashSet<>(ruleProcessorData.getNonGradReasons());
			List<GradRequirement> list = new ArrayList<>(s);
			ruleProcessorData.setNonGradReasons(list);

			ruleProcessorData.getNonGradReasons().sort(Comparator.comparing(GradRequirement::getRule, Comparator.nullsLast(String::compareTo)));
			graduationData.setNonGradReasons(ruleProcessorData.getNonGradReasons());
		}

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

	private void processGradMessages(boolean checkSCCPNOPROG, ExistingDataSupport existingDataSupport,Map<String, OptionalProgramRuleProcessor> mapOption,RuleProcessorData ruleProcessorData,GraduationData graduationData) {
		GradMessageRequest gradMessageRequest = GradMessageRequest.builder()
				.gradProgram(existingDataSupport.getGradProgram())
				.gradDate(graduationData.getGradStatus().getProgramCompletionDate())
				.honours(graduationData.getGradStatus().getHonoursStanding())
				.programName(ruleProcessorData.getGradProgram().getProgramName())
				.projected(ruleProcessorData.isProjected())
				.schoolAtGradName(graduationData.getGradStatus().getSchoolAtGradName())
				.existingGraduated(existingDataSupport.getExistingProgramCompletionDate() != null)
				.existingOptionalProgramGraduated(existingDataSupport.getOptionalNonGradReasons() == null || existingDataSupport.getOptionalNonGradReasons().isEmpty())
				.graduated(ruleProcessorData.isGraduated())
				.build();
		// GRAD2-2102: if a student is previously graduated, then treat it as graduated even though not graduated based on nonGradReasons by rule processor
		if(gradMessageRequest.isPreviouslyGraduated() || ruleProcessorData.isGraduated()) {
			gradMessageRequest.setMsgType(MSG_TYPE_GRADUATED);
		} else {
			gradMessageRequest.setMsgType(MSG_TYPE_NOT_GRADUATED);
		}
		graduationData.setGradMessage(getGradMessages(gradMessageRequest,mapOption,ruleProcessorData));

		if(checkSCCPNOPROG) {
			gradMessageRequest = GradMessageRequest.builder()
					.gradProgram(existingDataSupport.getGradProgram())
					.msgType(ruleProcessorData.isGraduated()? MSG_TYPE_GRADUATED : MSG_TYPE_NOT_GRADUATED)
					.gradDate(graduationData.getGradStatus().getProgramCompletionDate())
					.honours(graduationData.getGradStatus().getHonoursStanding())
					.programName(ruleProcessorData.getGradProgram().getProgramName())
					.projected(ruleProcessorData.isProjected())
					.existingGraduated(existingDataSupport.getExistingProgramCompletionDate() != null)
					.existingOptionalProgramGraduated(existingDataSupport.getOptionalNonGradReasons() == null || existingDataSupport.getOptionalNonGradReasons().isEmpty())
					.graduated(ruleProcessorData.isGraduated())
					.build();
			graduationData.setGradMessage(getGradMessages(gradMessageRequest,null,ruleProcessorData));
		}
	}

	private void createCompleteGradMessage(GradMessageRequest gradMessageRequest, StringBuilder currentGradMessage, TranscriptMessage result,
										   Map<String,OptionalProgramRuleProcessor> mapOptional, RuleProcessorData ruleProcessorData,String opMessage) {

		List<String> programs = new ArrayList<>();
		List<String> optPrograms = new ArrayList<>();
		String cpCommaSeparated = null;
		boolean dualDogwoodGraduated = false;
		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOptional.entrySet()) {
			String optionalProgramCode = entry.getKey();
			OptionalProgramRuleProcessor obj = entry.getValue();

			if(opMessage.equalsIgnoreCase(NON_GRADUATED)
				|| (obj.isOptionalProgramGraduated() && opMessage.equalsIgnoreCase(GRADUATED))) {
				if (optionalProgramCode.compareTo("AD") == 0 || optionalProgramCode.compareTo("BD") == 0 || optionalProgramCode.compareTo("BC") == 0) {
					programs.add(obj.getOptionalProgramName());
				}
				if (optionalProgramCode.compareTo("CP") == 0) {
					cpCommaSeparated = getCareerProgramNames(ruleProcessorData);
				}
				if (optionalProgramCode.compareTo("FI") == 0) {
					optPrograms.add(obj.getOptionalProgramName());
				}
				if(optionalProgramCode.compareTo("DD") == 0) {
					dualDogwoodGraduated = true;
				}
			}
		}

		if(!optPrograms.isEmpty() && opMessage.equalsIgnoreCase(GRADUATED)
			&& (!gradMessageRequest.isProjected() || gradMessageRequest.isExistingOptionalProgramGraduated())) {
			currentGradMessage.append(String.format(result.getFrenchImmersionMessage(), String.join(",", optPrograms)));
			appendPeriod(currentGradMessage);
		}
		if(ruleProcessorData.getGradProgram().getProgramCode().contains("-PF") && dualDogwoodGraduated && opMessage.equalsIgnoreCase(GRADUATED)) {
			currentGradMessage.append("Student has successfully completed the Programme Francophone");
			appendPeriod(currentGradMessage);
		}
		if(!programs.isEmpty() && StringUtils.isNotBlank(result.getAdIBProgramMessage())) {
			currentGradMessage.append(String.format(result.getAdIBProgramMessage(),String.join(",", programs)));
			appendPeriod(currentGradMessage);
		}
		if(StringUtils.isNotBlank(cpCommaSeparated) && StringUtils.isNotBlank(result.getCareerProgramMessage())) {
			currentGradMessage.append(String.format(result.getCareerProgramMessage(),cpCommaSeparated));
			appendPeriod(currentGradMessage);
		}

	}

	private String getCareerProgramNames(RuleProcessorData ruleProcessorData) {
		List<StudentCareerProgram> cpList = ruleProcessorData.getCpList();
		if(cpList != null) {
			return cpList.stream().map(cp -> String.valueOf(cp.getCareerProgramName())).collect(Collectors.joining(","));
		}
		return null;
	}

	Date toLastDayOfMonth(Date date) {
		if(date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			return cal.getTime();
		}
		return null;
	}
}
