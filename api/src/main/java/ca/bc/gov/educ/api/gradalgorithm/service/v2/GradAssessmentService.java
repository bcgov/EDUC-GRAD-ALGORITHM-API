package ca.bc.gov.educ.api.gradalgorithm.service.v2;

import ca.bc.gov.educ.api.gradalgorithm.constants.*;
import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentStudentListItem;
import ca.bc.gov.educ.api.gradalgorithm.mapper.AssessmentStudentAlgorithmMapper;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentAssessmentCacheService;
import ca.bc.gov.educ.api.gradalgorithm.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service("GradAssessmentServiceV2")
public class GradAssessmentService {

	private final AssessmentStudentAlgorithmMapper mapper = AssessmentStudentAlgorithmMapper.mapper;
	private final StudentAssessmentCacheService studentAssessmentCacheService;
	private final GradProgramService gradProgramCacheService;
	private final RestUtils restUtils;

	@Autowired
	public GradAssessmentService(StudentAssessmentCacheService studentAssessmentCacheService, GradProgramService gradProgramCacheService, RestUtils restUtils) {
    this.studentAssessmentCacheService = studentAssessmentCacheService;
    this.gradProgramCacheService = gradProgramCacheService;
    this.restUtils = restUtils;
	}

	Mono<AssessmentAlgorithmData> getAssessmentDataForAlgorithm(UUID studentID, ExceptionMessage exception) {
		try {
			// Get all data - if any fails, the entire operation fails
			List<AssessmentStudentListItem> studentAssessments = restUtils.sendMessageRequest(
					TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC,
					EventType.GET_ASSESSMENT_STUDENTS,
					String.valueOf(studentID),
          new TypeReference<>() {
          },
					Collections.emptyList()
			);
			List<StudentAssessment> algorithmStudentAssessments = studentAssessments.stream().map(mapper::toAlgorithmData).collect(Collectors.toCollection(ArrayList::new));

			List<AssessmentRequirement> assessmentRequirements = gradProgramCacheService.getAllAssessmentRequirements();

			List<Assessment> assessments = studentAssessmentCacheService.getAllAssessments();

			algorithmStudentAssessments.forEach(student -> student.setAssessmentName(studentAssessmentCacheService.getAssessmentNameByCode(student.getAssessmentCode())));

			AssessmentAlgorithmData result = new AssessmentAlgorithmData(algorithmStudentAssessments, assessmentRequirements, assessments);
			return Mono.just(prepareAssessmentDataForAlgorithm(result));
		} catch (Exception e) { //Left error handling at this level the same as V1 so algorithm functions the same
			exception.setExceptionName("Could not fetch algorithm assessment data");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	public AssessmentAlgorithmData prepareAssessmentDataForAlgorithm(AssessmentAlgorithmData result) {
		if(result != null && !result.getStudentAssessments().isEmpty()) {
			for (StudentAssessment studentAssessment : result.getStudentAssessments()) {
				studentAssessment.setGradReqMet("");
				studentAssessment.setGradReqMetDetail("");
			}

			log.debug("**** # of Student Assessments: {}", result.getStudentAssessments() != null ? result.getStudentAssessments().size() : 0);
			log.debug("**** # of Assessment Requirements: {}", result.getAssessmentRequirements() != null ? result.getAssessmentRequirements().size() : 0);
			log.debug("**** # of Assessments: {}", result.getAssessments() != null ? result.getAssessments().size() : 0);
		}
		return result;

	}
}
