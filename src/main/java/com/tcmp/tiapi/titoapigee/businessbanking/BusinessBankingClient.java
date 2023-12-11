package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequest;
import com.tcmp.tiapi.titoapigee.config.RequestBodyEncryptionConfiguration;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import feign.FeignException;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    contextId = "businessBankingContextId",
    value = "businessBankingClient",
    url = "${bp.api-gee.services.business-banking.url}",
    configuration = {RequestBodyEncryptionConfiguration.class})
public interface BusinessBankingClient {
  @PostMapping("/operational-gateway")
  @Retryable(
      retryFor = {FeignException.InternalServerError.class},
      backoff = @Backoff(delay = 1_000))
  Void notifyEvent(
      @RequestHeader Map<String, String> headers,
      @RequestBody ApiGeeBaseRequest<OperationalGatewayRequest<?>> body);
}
