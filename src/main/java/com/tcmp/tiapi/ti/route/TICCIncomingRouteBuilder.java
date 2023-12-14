package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.handler.TICCIncomingHandler;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class TICCIncomingRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormatAckEventRequest;
  private final TICCIncomingHandler ticcIncomingHandler;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("ticcIncomingQueue")
        .unmarshal(jaxbDataFormatAckEventRequest)
        .process()
        .body(AckServiceRequest.class, this::handleServiceRequest)
        .end();
  }

  private void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    String operation = serviceRequest.getHeader().getOperation();

    try {
      ticcIncomingHandler.strategy(operation).handleServiceRequest(serviceRequest);
    } catch (IllegalArgumentException e) {
      log.info("Unhandled operation: {}", operation);
      log.info("Body={}", serviceRequest);
    }
  }
}
