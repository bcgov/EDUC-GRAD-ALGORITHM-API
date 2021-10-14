package ca.bc.gov.educ.api.gradalgorithm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

@Service
public class GradCourseService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCourseService.class);
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private GradAlgorithmAPIConstants constants;
    
    CourseAlgorithmData getCourseDataForAlgorithm(String pen,String accessToken, ExceptionMessage exception) {
		exception = new ExceptionMessage();
		try
	    {
    		start();
	    	CourseAlgorithmData result = webClient.get()
	                .uri(String.format(constants.getCourseData(),pen))
	                .headers(h -> h.setBearerAuth(accessToken))
	                .retrieve()
	                .bodyToMono(CourseAlgorithmData.class)
	                .block();
	        end();
	        if(!result.getStudentCourses().isEmpty()) {
		        for (StudentCourse studentCourse : result.getStudentCourses()) {
		            studentCourse.setGradReqMet("");
		            studentCourse.setGradReqMetDetail("");
		        }
	        }
	        logger.info("**** # of Student Courses: " + (result.getStudentCourses() != null ? result.getStudentCourses().size() : 0));
	        logger.info("**** # of Course Requirements: " + (result.getCourseRequirements() != null ? result.getCourseRequirements().size() : 0));
	        logger.info("**** # of Course Restrictions: " + (result.getCourseRestrictions() != null ? result.getCourseRestrictions().size() : 0));
	        return result;
	    } catch (Exception e) {
	    	exception.setExceptionName("GRAD-COURSE-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
	    	return null;
	    }
    }
}