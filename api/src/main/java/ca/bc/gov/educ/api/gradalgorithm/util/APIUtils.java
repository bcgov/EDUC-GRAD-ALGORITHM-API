package ca.bc.gov.educ.api.gradalgorithm.util;

//import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;

public class APIUtils {

    public static HttpHeaders getHeaders (String username, String secret)
    {
        //String basicAuth = Base64.encodeBase64String((username + ":" + secret).getBytes());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Basic Z3JhZC1hcGktdXNlcjpTTjlXakB3WQ==");
        return httpHeaders;
    }

    public static HttpHeaders getHeaders (String accessToken)
    {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }

}
