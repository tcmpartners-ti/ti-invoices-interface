package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.handler.TICCIncomingHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/**
 * This route handles the results of successful operations such as: invoice creation, invoice
 * settlement, invoice financing, and invoice cancellation.
 */
@RequiredArgsConstructor
public class TICCIncomingRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormatAckEventRequest;
  private final TICCIncomingHandlerContext ticcIncomingHandlerContext;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("ticcIncomingQueue")
        .threads(2, 5)
        .unmarshal(jaxbDataFormatAckEventRequest)
        .process()
        .body(AckServiceRequest.class, this::handleServiceRequest)
        .end();
  }

  private void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    String operation = serviceRequest.getHeader().getOperation();

    try {
      TICCIncomingStrategy strategy = ticcIncomingHandlerContext.strategy(operation);
      strategy.handleServiceRequest(serviceRequest);
    } catch (IllegalArgumentException e) {
      log.info("Unhandled operation: {}", operation);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
