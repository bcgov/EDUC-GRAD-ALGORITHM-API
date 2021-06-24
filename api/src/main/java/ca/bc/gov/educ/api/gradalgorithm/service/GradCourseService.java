package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.CourseList;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRequirements;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseRestrictions;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_COURSE_REQUIREMENTS_API_URL;
import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.GRAD_COURSE_RESTRICTIONS_API_URL;

@Service
public class GradCourseService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradCourseService.class);

    @Autowired
    private WebClient webClient;

    CourseRequirements getAllCourseRequirements(List<StudentCourse> studentCourseList, String accessToken) {
        List<String> courseList = studentCourseList.stream()
                .map(StudentCourse::getCourseCode)
                .distinct()
                .collect(Collectors.toList());

        start();
        CourseRequirements result = webClient.post()
                .uri(GRAD_COURSE_REQUIREMENTS_API_URL + "/course-list")
                .headers(h -> h.setBearerAuth(accessToken))
                .body(BodyInserters.fromValue(new CourseList(courseList)))
                .retrieve()
                .bodyToMono(CourseRequirements.class)
                .block();
        end();

        logger.info("**** # of Course Requirements: " + (result != null ? result.getCourseRequirementList().size() : 0));
        return result;
    }

    CourseRestrictions getAllCourseRestrictions(List<StudentCourse> studentCourseList, String accessToken) {
        List<String> courseList = studentCourseList.stream()
                .map(StudentCourse::getCourseCode)
                .distinct()
                .collect(Collectors.toList());

        start();
        CourseRestrictions result = webClient.post()
                .uri(GRAD_COURSE_RESTRICTIONS_API_URL + "/course-list")
                .headers(h -> h.setBearerAuth(accessToken))
                .body(BodyInserters.fromValue(new CourseList(courseList)))
                .retrieve()
                .bodyToMono(CourseRestrictions.class)
                .block();
        end();

        logger.info("**** # of Course Restrictions: " + (result != null ? result.getCourseRestrictions().size() : 0));

        return result;
    }
}