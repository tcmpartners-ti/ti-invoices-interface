package com.tcmp.tiapi.ti;

import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.Credentials;
import com.tcmp.tiapi.ti.dto.request.ReplyFormat;
import com.tcmp.tiapi.ti.dto.request.RequestHeader;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;

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
