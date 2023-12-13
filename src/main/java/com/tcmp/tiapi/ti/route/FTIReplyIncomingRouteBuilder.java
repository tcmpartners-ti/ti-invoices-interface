package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.handler.FTIReplyIncomingHandler;
import com.tcmp.tiapi.ti.model.response.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class FTIReplyIncomingRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormatServiceResponse;
  private final FTIReplyIncomingHandler ftiReplyIncomingHandler;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("ftiReplyIncomingQueue")
        .unmarshal(jaxbDataFormatServiceResponse)
        .process()
        .body(ServiceResponse.class, this::handleServiceResponse)
        .end();
  }

  private void handleServiceResponse(ServiceResponse serviceResponse) {
    String operation = serviceResponse.getResponseHeader().getOperation();

    try {
      ftiReplyIncomingHandler.strategy(operation).handleServiceResponse(serviceResponse);
    } catch (IllegalArgumentException e) {
      log.info("Unhandled operation: {}", operation);
    }
  }
}
