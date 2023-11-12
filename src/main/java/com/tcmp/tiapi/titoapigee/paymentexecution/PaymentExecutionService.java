package com.tcmp.tiapi.titoapigee.paymentexecution;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExecutionService {
  private final HeaderSigner plainBodyRequestHeaderSigner;
  private final PaymentExecutionClient paymentExecutionClient;

  public BusinessAccountTransfersResponse makeTransactionRequest(TransactionRequest transactionRequest) {
    ApiGeeBaseRequest<TransactionRequest> request = new ApiGeeBaseRequest<>(transactionRequest);
    Map<String, String> headers = plainBodyRequestHeaderSigner.buildRequestHeaders(request);

    try {
      return paymentExecutionClient.postPayment(headers, request);
    } catch (FeignException e) {
      log.error("Could not execute transaction. {}", e.getMessage());
      throw new PaymentExecutionException("Could not create transaction.");
    }
  }
}
