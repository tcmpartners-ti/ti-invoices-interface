package com.tcmp.tiapi.titoapigee.operationalgateway;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.request.NotificationsRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.NotificationsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
  value = "operationalGatewayClient",
  url = "${bp.api-gee.services.operational-gateway.url}"
)
public interface OperationalGatewayClient {
  @PostMapping("/notifications")
  NotificationsResponse sendEmailNotification(
    @RequestHeader Map<String, String> headers,
    @RequestBody ApiGeeBaseRequest<NotificationsRequest> body
  );
}