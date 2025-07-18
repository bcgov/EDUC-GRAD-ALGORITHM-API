package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GradCourseService extends GradService {

	@Autowired
	public GradCourseService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	@Retry(name = "generalgetcall")
	Mono<CourseAlgorithmData> getCourseDataForAlgorithm(String pen, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
		{
			start();
			CourseAlgorithmData result = restService.get(String.format(constants.getCourseData(), pen),
					new ParameterizedTypeReference<CourseAlgorithmData>() {}, algorithmApiClient);
			end();
			return Mono.just(result);
		} catch (Exception e) {
			exception.setExceptionName("GRAD-COURSE-API IS DOWN");
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