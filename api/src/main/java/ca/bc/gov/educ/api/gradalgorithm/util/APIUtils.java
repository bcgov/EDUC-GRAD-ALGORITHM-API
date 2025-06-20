package ca.bc.gov.educ.api.gradalgorithm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Slf4j
public class APIUtils {

    private APIUtils() {}

    public static HttpHeaders getHeaders (String accessToken)
    {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }

    public static HttpHeaders getHeaders (String username,String password)
    {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setBasicAuth(username, password);
        return httpHeaders;
    }

    public static <T> String getJSONStringFromObject(T inputObject) {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        try {
            json = mapper.writeValueAsString(inputObject);
        } catch (JsonProcessingException e) {
            log.debug("ERROR {}",e.getLocalizedMessage());
        }

        return json;
    }

    /**
     * Remove characters from the CourseLevel if any (Example: 12A -> 12, 12B -> 12, etc..)
     * and Convert Course level into an integer
     *
     * @param courseLevel
     * @return Integer - Course Level
     */
    public static Integer getNumericCourseLevel(String courseLevel) {

        if (isBlankString(courseLevel))
            return 0;

        Integer cl = 0;
        Integer l = courseLevel.length();

        if (l > 2)
            cl = Integer.valueOf(
                    courseLevel.substring(0, l - 1)
            );
        else
            cl = Integer.valueOf(courseLevel);

        return cl;
    }

    /**
     *
     * @param string
     * @return true or false
     */
    public static boolean isBlankString(String string) {
        return string == null || string.trim().isEmpty();
    }
}
