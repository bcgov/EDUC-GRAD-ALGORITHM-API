package ca.bc.gov.educ.api.gradalgorithm.util;

import ca.bc.gov.educ.api.gradalgorithm.constants.EventType;
import ca.bc.gov.educ.api.gradalgorithm.constants.TopicsEnum;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.Event;
import ca.bc.gov.educ.api.gradalgorithm.exception.ServiceException;
import ca.bc.gov.educ.api.gradalgorithm.messaging.MessagePublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import io.nats.client.Message;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestUtilsTest {

    @Mock
    private MessagePublisher messagePublisher;

    @Mock
    private CompletableFuture<Message> completableFuture;
    
    @Mock
    private Message mockMessage;

    private RestUtils restUtils;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        restUtils = new RestUtils(messagePublisher);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testSendMessageRequest_WithValidResponse_ShouldReturnData() throws Exception {
        // Given
        String testPayload = "test-payload";
        String expectedResponse = "test-response";
        byte[] responseData = objectMapper.writeValueAsBytes(expectedResponse);
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(mockMessage.getData()).thenReturn(responseData);
        when(completableFuture.get()).thenReturn(mockMessage);

        // When
        String result = restUtils.sendMessageRequest(
            TopicsEnum.GRAD_STUDENT_API_TOPIC,
            EventType.GET_STUDENT_COURSES,
            testPayload,
            new TypeReference<>() {
            },
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(messagePublisher).requestMessage(eq(TopicsEnum.GRAD_STUDENT_API_TOPIC.toString()), any(byte[].class));
    }

    @Test
    public void testSendMessageRequest_WithNullResponse_ShouldThrowServiceException() throws Exception {
        // Given
        String testPayload = "test-payload";
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(completableFuture.get()).thenReturn(null);

        // When & Then
        try {
            restUtils.sendMessageRequest(
                TopicsEnum.GRAD_STUDENT_API_TOPIC,
                EventType.GET_STUDENT_COURSES,
                testPayload,
                new TypeReference<String>() {},
                null
            );
            fail("Expected ServiceException to be thrown");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("NATS request timed out"));
            assertTrue(e.getMessage().contains("GET_STUDENT_COURSES"));
        }
    }

    @Test
    public void testSendMessageRequest_WithEmptyResponseData_ShouldReturnDefaultValue() throws Exception {
        // Given
        String testPayload = "test-payload";
        String defaultValue = "default-value";
        byte[] emptyResponseData = new byte[0];
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(mockMessage.getData()).thenReturn(emptyResponseData);
        when(completableFuture.get()).thenReturn(mockMessage);

        // When
        String result = restUtils.sendMessageRequest(
            TopicsEnum.GRAD_STUDENT_API_TOPIC,
            EventType.GET_STUDENT_COURSES,
            testPayload,
            new TypeReference<>() {
            },
            defaultValue
        );

        // Then
        assertNotNull(result);
        assertEquals(defaultValue, result);
    }

    @Test
    public void testSendMessageRequest_WithEmptyResponseDataAndNoDefault_ShouldThrowEntityNotFoundException() throws Exception {
        // Given
        String testPayload = "test-payload";
        byte[] emptyResponseData = new byte[0];
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(mockMessage.getData()).thenReturn(emptyResponseData);
        when(completableFuture.get()).thenReturn(mockMessage);

        // When & Then
        try {
            restUtils.sendMessageRequest(
                TopicsEnum.GRAD_STUDENT_API_TOPIC,
                EventType.GET_STUDENT_COURSES,
                testPayload,
                new TypeReference<String>() {},
                null
            );
            fail("Expected EntityNotFoundException to be thrown");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("java.lang.String"));
        }
    }

    @Test
    public void testSendMessageRequest_WithException_ShouldThrowServiceException() throws Exception {
        // Given
        String testPayload = "test-payload";
        String errorMessage = "Connection failed";
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(completableFuture.get()).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        try {
            restUtils.sendMessageRequest(
                TopicsEnum.GRAD_STUDENT_API_TOPIC,
                EventType.GET_STUDENT_COURSES,
                testPayload,
                new TypeReference<String>() {},
                null
            );
            fail("Expected ServiceException to be thrown");
        } catch (ServiceException e) {
            assertTrue(e.getMessage().contains("Failed to retrieve GET_STUDENT_COURSES data"));
            assertTrue(e.getMessage().contains(errorMessage));
        }
    }

    @Test
    public void testSendMessageRequest_WithValidJsonResponse_ShouldDeserializeCorrectly() throws Exception {
        // Given
        String testPayload = "test-payload";
        TestResponse expectedResponse = new TestResponse("test-id", "test-name");
        byte[] responseData = objectMapper.writeValueAsBytes(expectedResponse);
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(mockMessage.getData()).thenReturn(responseData);
        when(completableFuture.get()).thenReturn(mockMessage);

        // When
        TestResponse result = restUtils.sendMessageRequest(
            TopicsEnum.GRAD_STUDENT_API_TOPIC,
            EventType.GET_STUDENT_COURSES,
            testPayload,
            new TypeReference<>() {
            },
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getName(), result.getName());
    }

    @Test
    public void testSendMessageRequest_WithDifferentEventTypes_ShouldCreateCorrectEvent() throws Exception {
        // Given
        String testPayload = "test-payload";
        String expectedResponse = "test-response";
        byte[] responseData = objectMapper.writeValueAsBytes(expectedResponse);
        
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
            .thenReturn(completableFuture);
        when(completableFuture.completeOnTimeout(any(), anyLong(), any(TimeUnit.class)))
            .thenReturn(completableFuture);
        when(mockMessage.getData()).thenReturn(responseData);
        when(completableFuture.get()).thenReturn(mockMessage);

        // When
        restUtils.sendMessageRequest(
            TopicsEnum.GRAD_STUDENT_API_TOPIC,
            EventType.GET_ASSESSMENT_STUDENTS,
            testPayload,
            new TypeReference<String>() {},
            null
        );

        // Then
        verify(messagePublisher).requestMessage(anyString(), argThat(bytes -> {
            try {
                Event event = objectMapper.readValue(bytes, Event.class);
                return event.getEventType() == EventType.GET_ASSESSMENT_STUDENTS &&
                       testPayload.equals(event.getEventPayload()) &&
                       event.getSagaId() != null;
            } catch (Exception e) {
                return false;
            }
        }));
    }

    // Helper class for testing
    @Getter
    public static class TestResponse {
        private String id;
        @Setter
        private String name;

        public TestResponse() {}

        public TestResponse(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
