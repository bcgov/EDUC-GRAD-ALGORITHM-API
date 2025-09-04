package ca.bc.gov.educ.api.gradalgorithm.service.v2;
import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.constants.TopicsEnum;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRequirement;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRestriction;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;

import ca.bc.gov.educ.api.gradalgorithm.util.RestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("GradCourseServiceV2")
@Slf4j
public class GradCourseService {

  private final RestUtils restUtils;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public GradCourseService(RestUtils restUtils) {
    this.restUtils = restUtils;
  }

  Mono<CourseAlgorithmData> getCourseDataForAlgorithm(UUID studentID, ExceptionMessage exception) {
    try {
      List<StudentCourse> studentCourses = restUtils.sendMessageRequest(
          TopicsEnum.GRAD_STUDENT_API_TOPIC,
          EventType.GET_STUDENT_COURSES,
          String.valueOf(studentID),
          new TypeReference<>() {
          },
          null
      );

      List<String> courseCodes = studentCourses.stream()
          .map(StudentCourse::getCourseCode)
          .distinct()
          .collect(Collectors.toCollection(ArrayList::new));

      List<CourseRequirement> courseRequirements = restUtils.sendMessageRequest(
          TopicsEnum.GRAD_COURSE_API_TOPIC,
          EventType.GET_COURSE_REQUIREMENTS,
          objectMapper.writeValueAsString(courseCodes), // Pass course codes as payload
          new TypeReference<>() {
          },
          null
      );

      List<CourseRestriction> courseRestrictions = restUtils.sendMessageRequest(
          TopicsEnum.GRAD_COURSE_API_TOPIC,
          EventType.GET_COURSE_RESTRICTIONS,
          objectMapper.writeValueAsString(courseCodes), // Pass course codes as payload
          new TypeReference<>() {
          },
          null
      );

      CourseAlgorithmData result = new CourseAlgorithmData(studentCourses, courseRequirements, courseRestrictions);
      return Mono.just(prepareCourseDataForAlgorithm(result));

    } catch (Exception e) { //Left error handling at this level the same as V1 so algorithm functions the same
      exception.setExceptionName("Could not fetch algorithm courses data");
      exception.setExceptionDetails(e.getLocalizedMessage());
      return null;
    }
  }

  CourseAlgorithmData prepareCourseDataForAlgorithm(CourseAlgorithmData result) {
    if(result != null && !result.getStudentCourses().isEmpty()) {
      for (StudentCourse studentCourse : result.getStudentCourses()) {
        studentCourse.setGradReqMet("");
        studentCourse.setGradReqMetDetail("");
      }

      log.debug("**** # of Student Courses: {} ",result.getStudentCourses() != null ? result.getStudentCourses().size() : 0);
      log.debug("**** # of Course Requirements: {}",result.getCourseRequirements() != null ? result.getCourseRequirements().size() : 0);
      log.debug("**** # of Course Restrictions: {}",result.getCourseRestrictions() != null ? result.getCourseRestrictions().size() : 0);
    }
    return result;
  }
}