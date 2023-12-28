package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.ti.handler.FTIReplyIncomingHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/**
 * This route receives the "validations" results from operations such as: Invoice creation and
 * Invoice Financing. E.g.: if the program/seller/buyer relationship is not correct, the error will
 * be caught in this route.
 */
@RequiredArgsConstructor
public class FTIReplyIncomingRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormatServiceResponse;
  private final FTIReplyIncomingHandlerContext ftiReplyIncomingHandlerContext;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("ftiReplyIncomingQueue")
        .threads(5, 10)
        .unmarshal(jaxbDataFormatServiceResponse)
        .process()
        .body(ServiceResponse.class, this::handleServiceResponse)
        .end();
  }

  private void handleServiceResponse(ServiceResponse serviceResponse) {
    String operation = serviceResponse.getResponseHeader().getOperation();

    try {
      FTIReplyIncomingStrategy strategy = ftiReplyIncomingHandlerContext.strategy(operation);
      strategy.handleServiceResponse(serviceResponse);
    } catch (IllegalArgumentException e) {
      log.info("Unhandled operation: {}", operation);
    }
  }
}
