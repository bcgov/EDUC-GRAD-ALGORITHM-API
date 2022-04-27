package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.ExceptionMessage;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradShoolServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(GradShoolServiceTests.class);
    private static final String CLASS_NAME = GradShoolServiceTests.class.getSimpleName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired GradSchoolService gradSchoolService;
    @Autowired ExceptionMessage exception;
    @MockBean WebClient webClient;
    
    @Autowired GradAlgorithmAPIConstants constants;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.ResponseSpec responseMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;

    @BeforeClass
    public static void setup() {

    }

    @After
    public void tearDown() {

    }

    @Before
    public void init() {
        openMocks(this);
    }

    @Test
    public void getSchoolTest() {
        LOG.debug("<{}.getSchoolTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String mincode = "08098655";
        String accessToken = "accessToken";

        School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName("My School");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincode(), mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        Mono<School> result = gradSchoolService.getSchool(mincode, accessToken,exception);
        assertNotNull(result.block());
        LOG.debug(">getSchoolTest");
    }
    
    @Test
    public void getSchoolTest_withexception() {
        LOG.debug("<{}.getSchoolTest at {}", CLASS_NAME, dateFormat.format(new Date()));
        String mincode = "08098655";
        String accessToken = "accessToken";

        School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName("My School");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincode(), mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));

        Mono<School> result = gradSchoolService.getSchool(mincode, accessToken,exception);
        assertNull(result);
        LOG.debug(">getSchoolTest");
    }
}
