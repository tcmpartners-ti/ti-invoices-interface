package com.tcmp.tiapi.titoapigee.paymentexecution;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.shared.exception.SimpleFeignException;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentExecutionServiceTest {
  @Mock private ObjectMapper objectMapper;
  @Mock private HeaderSigner plainBodyRequestHeaderSigner;
  @Mock private PaymentExecutionClient paymentExecutionClient;

  private PaymentExecutionService paymentExecutionService;

  @BeforeEach
  void setUp() {
    paymentExecutionService = new PaymentExecutionService(
      objectMapper,
      plainBodyRequestHeaderSigner,
      paymentExecutionClient
    );
  }

  @Test
  void makeTransactionRequest_itShouldThrowException() {
    var request = TransactionRequest.builder().build();

    when(plainBodyRequestHeaderSigner.buildRequestHeaders(any()))
      .thenReturn(Map.of());
    when(paymentExecutionClient.postPayment(anyMap(), any()))
      .thenThrow(new SimpleFeignException(400, "Bad Request"));

    assertThrows(
      PaymentExecutionException.class,
      () -> paymentExecutionService.makeTransactionRequest(request)
    );
  }

  @Test
  void makeTransactionRequest_itShouldReturnResponse() {
    var request = TransactionRequest.builder().build();

    when(plainBodyRequestHeaderSigner.buildRequestHeaders(any()))
      .thenReturn(Map.of());
    when(paymentExecutionClient.postPayment(anyMap(), any()))
      .thenReturn(new BusinessAccountTransfersResponse(null));

    var actualResponse = paymentExecutionService.makeTransactionRequest(request);

    assertNotNull(actualResponse);
  }
}
