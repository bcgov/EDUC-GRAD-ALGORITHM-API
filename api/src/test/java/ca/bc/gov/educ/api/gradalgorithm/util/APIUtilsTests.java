package ca.bc.gov.educ.api.gradalgorithm.util;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class APIUtilsTests {

    @Test
    void getHeadersTest() {
        HttpHeaders headers;
        headers = APIUtils.getHeaders("my-access-token");
        assertEquals("application/json", headers.get("Content-Type").get(0));
    }

    @Test
    void getNumericCourseLevel_WithSingleDigitAndLetter_ShouldReturnNumeric() {
        assertEquals(1, APIUtils.getNumericCourseLevel("1A"));
    }

    @Test
    void getNumericCourseLevel_WithDoubleDigitAndLetter_ShouldReturnNumeric() {
        assertEquals(12, APIUtils.getNumericCourseLevel("12A"));
    }

    @Test
    void getNumericCourseLevel_WithNumericOnly_ShouldReturnNumeric() {
        assertEquals(123, APIUtils.getNumericCourseLevel("123"));
    }

    @Test
    void getNumericCourseLevel_WithNullOrEmpty_ShouldReturnZero() {
        assertEquals(0, APIUtils.getNumericCourseLevel(null));
        assertEquals(0, APIUtils.getNumericCourseLevel(""));
        assertEquals(0, APIUtils.getNumericCourseLevel("A"));
    }
}
