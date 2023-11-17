package com.tcmp.tiapi.titoapigee.paymentexecution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseError;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExecutionService {
  private static final String DEFAULT_ERROR_MESSAGE = "Could not execute transaction";

  private final ObjectMapper objectMapper;
  private final HeaderSigner plainBodyRequestHeaderSigner;
  private final PaymentExecutionClient paymentExecutionClient;

  public BusinessAccountTransfersResponse makeTransactionRequest(TransactionRequest transactionRequest) {
    ApiGeeBaseRequest<TransactionRequest> request = new ApiGeeBaseRequest<>(transactionRequest);
    Map<String, String> headers = plainBodyRequestHeaderSigner.buildRequestHeaders(request);

    try {
      BusinessAccountTransfersResponse response = paymentExecutionClient.postPayment(headers, request);
      tryRequestAndResponseLogging(request, response);

      return response;
    } catch (FeignException e) {
      log.error("Could not execute transaction. {}", e.getMessage());

      throw new PaymentExecutionException(
        DEFAULT_ERROR_MESSAGE,
        e,
        tryBuildTransferErrorFromExceptionOrDefault(e)
      );
    }
  }

  private void tryRequestAndResponseLogging(ApiGeeBaseRequest<?> request, Object response) {
    try {
      log.info("Request={}", objectMapper.writeValueAsString(request));
      log.info("Response={}", objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      log.error("Could not log request and response json.");
    }
  }

  private TransferResponseError tryBuildTransferErrorFromExceptionOrDefault(FeignException e) {
    final TransferResponseError defaultResponseError = TransferResponseError.builder()
      .title(DEFAULT_ERROR_MESSAGE)
      .build();

    try {
      ByteBuffer body = e.responseBody().orElseThrow(IOException::new);
      return objectMapper.readValue(body.array(), TransferResponseError.class);
    } catch (IOException ex) {
      return defaultResponseError;
    }
  }
}
