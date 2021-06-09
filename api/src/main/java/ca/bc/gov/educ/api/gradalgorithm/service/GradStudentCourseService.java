package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.StudentCourse;
import ca.bc.gov.educ.api.gradalgorithm.util.APIUtils;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GradStudentCourseService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentCourseService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    StudentCourse[] getAllCoursesForAStudentWithRestTemplate(String pen, String accessToken) {

        HttpHeaders httpHeaders = APIUtils.getHeaders(accessToken);

        start();
        ResponseEntity<StudentCourse[]> response = restTemplate.exchange(
                GradAlgorithmAPIConstants.GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), StudentCourse[].class);
        end();

        StudentCourse[] result = new StudentCourse[0];

        if (response.getStatusCode().value() != 204)
            result = response.getBody();

        logger.info("**** # of courses: " + (result != null ? result.length : 0));
        for (StudentCourse studentCourse : result) {
            studentCourse.setGradReqMet("");
            studentCourse.setGradReqMetDetail("");
        }

        return result;
    }

    StudentCourse[] getAllCoursesForAStudent(String pen, String accessToken) {
        start();
        StudentCourse[] result = webClient.get()
                .uri(GradAlgorithmAPIConstants.GET_STUDENT_COURSES_BY_PEN_URL + "/" + pen)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<StudentCourse[]>(){})
                .block();
        end();

        logger.info("**** # of courses: " + (result != null ? result.length : 0));

        for (StudentCourse studentCourse : result) {
            studentCourse.setGradReqMet("");
            studentCourse.setGradReqMetDetail("");
        }

        return result;
    }
}
