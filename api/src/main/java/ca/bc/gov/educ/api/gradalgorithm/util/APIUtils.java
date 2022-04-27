package ca.bc.gov.educ.api.gradalgorithm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class APIUtils {

    private APIUtils() {}

    public static HttpHeaders getHeaders (String accessToken)
    {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }

    public static <T> String getJSONStringFromObject(T inputObject) {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";

        try {
            json = mapper.writeValueAsString(inputObject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static String parsingTraxDate(String pcDate) {
        String actualPCDate = pcDate + "/01";
        Date gradDate=null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            gradDate = dateFormat.parse(actualPCDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(gradDate);
    }
}
