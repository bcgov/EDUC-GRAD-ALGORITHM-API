package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.Student;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GradAlgorithmServiceTests {

    GradAlgorithmService gradAlgorithmService;

    @Mock
    RestTemplate restTemplateMock;

    @BeforeEach
    void init() {
        gradAlgorithmService = new GradAlgorithmService();
    }

    @Test
    void getStudentDemographicsTest() {

        GradStudent gradStudent = new GradStudent();
        gradStudent.setStudGiven("NAVJEEVAN");
        assertEquals("NAVJEEVAN", gradStudent.getStudGiven(), "Given Name of the student doesn't match");
    }
}
