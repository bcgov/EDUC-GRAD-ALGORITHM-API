package ca.bc.gov.educ.api.gradalgorithm.util;

import org.springframework.http.HttpHeaders;

public class APIUtils {

	public static HttpHeaders getHeaders (String accessToken)
    {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }

}
