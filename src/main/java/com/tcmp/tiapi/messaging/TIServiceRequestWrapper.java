package com.tcmp.tiapi.messaging;

import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.Credentials;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.model.requests.RequestHeader;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;

public class TIServiceRequestWrapper {
  public <T> ServiceRequest<T> wrapRequest(
      TIService service,
      TIOperation operation,
      ReplyFormat replyFormat,
      String correlationId,
      T requestBody) {
    RequestHeader requestHeader =
        RequestHeader.builder()
            .service(service.getValue())
            .operation(operation.getValue())
            .replyFormat(replyFormat.getValue())
            .noOverride("N")
            .correlationId(correlationId)
            .credentials(Credentials.builder().name("TI_INTERFACE").build())
            .build();

    return ServiceRequest.<T>builder().header(requestHeader).body(requestBody).build();
  }
}
