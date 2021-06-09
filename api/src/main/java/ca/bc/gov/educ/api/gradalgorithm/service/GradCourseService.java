package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.struct.CourseList;
import ca.bc.gov.educ.api.gradalgorithm.struct.CourseRequirements;
import ca.bc.gov.educ.api.gradalgorithm.struct.CourseRestrictions;
import ca.bc.gov.educ.api.gradalgorithm.struct.StudentCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradalgorithm.util.APIUtils.getJSONStringFromObject;

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
                .uri("https://grad-course-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/course/course-requirement/course-list")
                .header("Authorization", "Bearer " + accessToken)
                .body(Mono.just(new CourseList(courseList)), CourseList.class)
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
                .uri("https://grad-course-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v1/course/course-restriction/course-list")
                .header("Authorization", "Bearer " + accessToken)
                .body(Mono.just(new CourseList(courseList)), CourseList.class)
                .retrieve()
                .bodyToMono(CourseRestrictions.class)
                .block();
        end();

        logger.info("**** # of Course Restrictions: " + (result != null ? result.getCourseRestrictions().size() : 0));

        return result;
    }
}