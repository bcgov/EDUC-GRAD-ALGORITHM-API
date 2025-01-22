package ca.bc.gov.educ.api.gradalgorithm.util;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;

class APIUtilsTests {

    @Test
    void getHeadersTest() {
        HttpHeaders headers;
        headers = APIUtils.getHeaders("my-access-token");
        assertEquals("application/json", headers.get("Content-Type").get(0));
    }
}
