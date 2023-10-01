package com.tcmp.tiapi.titoapigee.paymentexecution;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
  value = "paymentExecutionClient",
  url = "${bp.api-gee.services.payment-execution.url}"
)
public interface PaymentExecutionClient {
  @PostMapping("/bussines-account-transfers/customer")
  BusinessAccountTransfersResponse postPayment(
    @RequestHeader Map<String, String> headers,
    @RequestBody ApiGeeBaseRequest<TransactionRequest> body
  );
}