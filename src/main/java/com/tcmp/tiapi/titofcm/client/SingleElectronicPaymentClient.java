package com.tcmp.tiapi.titofcm.client;

import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    contextId = "singleElectronicPaymentContextId",
    value = "singleElectronicPaymentClient",
    url = "${fcm.api.services.single-electronic-payment.url}")
public interface SingleElectronicPaymentClient {
  @PostMapping("/single-payment")
  SinglePaymentResponse createSinglePayment(
      @RequestHeader Map<String, String> headers, @RequestBody SinglePaymentRequest body);
}
