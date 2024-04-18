package com.tcmp.tiapi.titofcm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.titofcm.client.SingleElectronicPaymentClient;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import feign.FeignException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SingleElectronicPaymentServiceTest {
  @Mock private ObjectMapper objectMapper;
  @Mock private UUIDGenerator uuidGenerator;
  @Mock private SingleElectronicPaymentClient singleElectronicPaymentClient;

  @Captor private ArgumentCaptor<Map<String, String>> headerArgumentCaptor;

  @InjectMocks private SingleElectronicPaymentService singleElectronicPaymentService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(singleElectronicPaymentService, "userIdHeader", "1192150");
  }

  @Test
  void createSinglePayment_itShouldThrowExceptionOnFail() throws JsonProcessingException {
    var mockBadRequestException = mock(FeignException.class);

    when(uuidGenerator.getNewId()).thenReturn("7e0843e3-4bc2-4df1-a7b5-6c88c742ce78");

    when(singleElectronicPaymentClient.createSinglePayment(any(), any(SinglePaymentRequest.class)))
        .thenThrow(mockBadRequestException);

    var request = new SinglePaymentRequest();
    assertThrows(
        SinglePaymentException.class,
        () -> singleElectronicPaymentService.createSinglePayment(request));

    verify(objectMapper).writeValueAsString(any(Object.class));
  }

  @Test
  void createSinglePayment_itShouldReturnResponse() throws JsonProcessingException {
    when(uuidGenerator.getNewId()).thenReturn("7e0843e3-4bc2-4df1-a7b5-6c88c742ce78");
    when(singleElectronicPaymentClient.createSinglePayment(any(), any()))
        .thenReturn(new SinglePaymentResponse(new SinglePaymentResponse.Data("")));

    var response = singleElectronicPaymentService.createSinglePayment(new SinglePaymentRequest());

    verify(objectMapper, times(2)).writeValueAsString(any(Object.class));
    verify(singleElectronicPaymentClient)
        .createSinglePayment(headerArgumentCaptor.capture(), any(SinglePaymentRequest.class));

    var expectedRequestId = "7e0843e34bc24df1a7b5";
    assertNotNull(response);
    assertEquals(expectedRequestId, headerArgumentCaptor.getValue().get("X-Request-ID"));
  }
}
