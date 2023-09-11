package com.tcmp.tiapi.titoapigee.client;

import com.tcmp.tiapi.titoapigee.dto.request.OperationalGatewayRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  value = "operationalGatewayClient",
  url = "${bp.api-gee.services.business-banking.operational-gateway.url}"
)
public interface OperationalGatewayClient {
  @PostMapping
  Object sendInvoiceCreationResult(@RequestBody OperationalGatewayRequest body);
}

