package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class GradCourseService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCourseService.class);
    
    private WebClient webClient;
    
    private GradAlgorithmAPIConstants constants;

	@Retry(name = "generalgetcall")
	Mono<CourseAlgorithmData> getCourseDataForAlgorithm(String pen,String accessToken, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
		{
			start();
			Mono<CourseAlgorithmData> result = webClient.get()
					.uri(String.format(constants.getCourseData(),pen))
					.headers(h -> h.setBearerAuth(accessToken))
					.retrieve()
					.bodyToMono(CourseAlgorithmData.class);
			end();
			return result;
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

			logger.debug("**** # of Student Courses: {} ",result.getStudentCourses() != null ? result.getStudentCourses().size() : 0);
			logger.debug("**** # of Course Requirements: {}",result.getCourseRequirements() != null ? result.getCourseRequirements().size() : 0);
			logger.debug("**** # of Course Restrictions: {}",result.getCourseRestrictions() != null ? result.getCourseRestrictions().size() : 0);
		}
		return result;
	}

}