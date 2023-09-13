package com.tcmp.tiapi.titoapigee.client;

import com.tcmp.tiapi.titoapigee.configuration.ApiGeeConfiguration;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.dto.request.OperationalGatewayRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
  value = "operationalGatewayClient",
  url = "${bp.api-gee.services.business-banking.operational-gateway.url}",
  configuration = ApiGeeConfiguration.class
)
public interface OperationalGatewayClient {
  @PostMapping
  Void sendInvoiceCreationResult(
    @RequestHeader Map<String, String> headers,
    @RequestBody ApiGeeBaseRequest<OperationalGatewayRequest> body
  );
}

