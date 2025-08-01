package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.GraduationData;
import ca.bc.gov.educ.api.gradalgorithm.exception.ServiceException;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradProgramService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.GradSchoolService;
import ca.bc.gov.educ.api.gradalgorithm.service.caching.StudentGraduationService;
import io.netty.channel.ConnectTimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RESTServiceGETTest {

    @Autowired
    private RESTService restService;
    @MockBean(name = "algorithmApiClient")
    @Qualifier("algorithmApiClient")
    WebClient algorithmApiClient;

    @MockBean
    GradProgramService gradProgramService;
    @MockBean
    GradSchoolService gradSchoolService;
    @MockBean
    StudentGraduationService studentGraduationService;

    @MockBean
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @MockBean
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @MockBean
    private WebClient.RequestBodySpec requestBodyMock;
    @MockBean
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @MockBean
    private WebClient.ResponseSpec responseMock;
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepositoryMock;
    @MockBean
    private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepositoryMock;

    private static final String TEST_URL_200 = "https://httpstat.us/200";
    private static final String TEST_URL_403 = "https://httpstat.us/403";
    private static final String TEST_URL_503 = "https://httpstat.us/503";
    private static final String OK_RESPONSE = "200 OK";
    private static final ParameterizedTypeReference<List<GraduationData>> refType = new ParameterizedTypeReference<List<GraduationData>>() {};

    @Before
    public void setUp(){
        when(this.algorithmApiClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(any(String.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
    }

    @Test
    public void testGet_GivenNullWebClient_Expect200Response(){
        when(this.responseMock.bodyToMono(String.class)).thenReturn(Mono.just(OK_RESPONSE));
        String response = this.restService.get(TEST_URL_200, String.class, null);
        Assert.assertEquals(OK_RESPONSE, response);
    }

    @Test
    public void testGetTypeRef_GivenProperData_Expect200Response(){
        when(this.responseMock.bodyToMono(refType)).thenReturn(Mono.just(new ArrayList<GraduationData>()));
        List<GraduationData> response = this.restService.get(TEST_URL_200, refType, algorithmApiClient);
        Assert.assertEquals(new ArrayList<String>(), response);
    }

    @Test
    public void testGetTypeRef_GivenNullWebClient_Expect200Response(){
        when(this.responseMock.bodyToMono(refType)).thenReturn(Mono.just(new ArrayList<GraduationData>()));
        List<GraduationData> response = this.restService.get(TEST_URL_200, refType, null);
        Assert.assertEquals(new ArrayList<String>(), response);
    }

    @Test(expected = ServiceException.class)
    public void testGetTypeRef_Given5xxErrorFromService_ExpectServiceError(){
        when(this.responseMock.bodyToMono(refType)).thenThrow(new ServiceException());
        this.restService.get(TEST_URL_503, refType, algorithmApiClient);
    }

    @Test(expected = ServiceException.class)
    public void testGet_Given4xxErrorFromService_ExpectServiceError(){
        when(this.responseMock.bodyToMono(ServiceException.class)).thenReturn(Mono.just(new ServiceException()));
        this.restService.get(TEST_URL_403, String.class, algorithmApiClient);
    }

    @Test(expected = ServiceException.class)
    public void testGetTypeRef_Given4xxErrorFromService_ExpectServiceError(){
        when(this.responseMock.bodyToMono(ServiceException.class)).thenReturn(Mono.just(new ServiceException()));
        this.restService.get(TEST_URL_403, refType, algorithmApiClient);
    }

    @Test(expected = ServiceException.class)
    public void testGet_Given5xxErrorFromService_ExpectConnectionError(){
        when(requestBodyUriMock.uri(TEST_URL_503)).thenReturn(requestBodyMock);
        when(requestBodyMock.retrieve()).thenReturn(responseMock);

        when(responseMock.bodyToMono(String.class)).thenReturn(Mono.error(new ConnectTimeoutException("Connection closed")));
        restService.get(TEST_URL_503, String.class, algorithmApiClient);
    }

    @Test(expected = ServiceException.class)
    public void testGet_Given5xxErrorFromService_ExpectWebClientRequestError(){
        when(requestBodyUriMock.uri(TEST_URL_503)).thenReturn(requestBodyMock);
        when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Throwable cause = new RuntimeException("Simulated cause");
        when(responseMock.bodyToMono(String.class)).thenReturn(Mono.error(new WebClientRequestException(cause, HttpMethod.GET, null, new HttpHeaders())));
        restService.get(TEST_URL_503, String.class, algorithmApiClient);
    }

    @Test(expected = ServiceException.class)
    public void testGetTypeRef_Given5xxErrorFromService_ExpectWebClientRequestError(){
        when(requestBodyUriMock.uri(TEST_URL_503)).thenReturn(requestBodyMock);
        when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Throwable cause = new RuntimeException("Simulated cause");
        when(responseMock.bodyToMono(String.class)).thenReturn(Mono.error(new WebClientRequestException(cause, HttpMethod.GET, null, new HttpHeaders())));
        restService.get(TEST_URL_503, refType, algorithmApiClient);
    }

    @Test
    public void testGet_GivenProperData_Expect200Response(){
        when(this.responseMock.bodyToMono(String.class)).thenReturn(Mono.just(OK_RESPONSE));
        String response = this.restService.get(TEST_URL_200, String.class, algorithmApiClient);
        assertEquals("200 OK", response);
    }

    @Test
    public void testGetOverride_GivenProperData_Expect200Response(){
        when(this.responseMock.bodyToMono(String.class)).thenReturn(Mono.just(OK_RESPONSE));
        String response = this.restService.get(TEST_URL_200, String.class, algorithmApiClient);
        assertEquals(OK_RESPONSE, response);
    }

    @Test(expected = ServiceException.class)
    public void testGet_Given5xxErrorFromService_ExpectServiceError(){
        when(this.responseMock.bodyToMono(ServiceException.class)).thenReturn(Mono.just(new ServiceException()));
        this.restService.get(TEST_URL_503, String.class, algorithmApiClient);
    }
}
