package com.tcmp.tiapi.ti.route.fti;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.ti.handler.FTIReplyIncomingHandlerContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/**
 * This route receives the "validations" results from operations such as: Invoice creation and
 * Invoice Financing. E.g.: if the program/seller/buyer relationship is not correct, the error will
 * be received in this route.
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
        .threads(2, 5)
        .unmarshal(jaxbDataFormatServiceResponse)
        .process()
        .body(ServiceResponse.class, this::handleServiceResponse)
        .end();
  }

  private void handleServiceResponse(ServiceResponse serviceResponse) {
    String operation = serviceResponse.getResponseHeader().getOperation();
    printDetailsIfPresent(serviceResponse);

    try {
      FTIReplyIncomingStrategy strategy = ftiReplyIncomingHandlerContext.strategy(operation);
      strategy.handleServiceResponse(serviceResponse);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private void printDetailsIfPresent(ServiceResponse serviceResponse) {
    List<String> errors = serviceResponse.getResponseHeader().getDetails().getErrors();
    List<String> warnings = serviceResponse.getResponseHeader().getDetails().getWarnings();
    List<String> information = serviceResponse.getResponseHeader().getDetails().getInfos();

    if (errors != null && !errors.isEmpty()) {
      log.error("Errors: {}", errors);
    }

    if (warnings != null && !warnings.isEmpty()) {
      log.error("Warnings: {}", warnings);
    }

    if (information != null && !information.isEmpty()) {
      log.error("Information: {}", information);
    }
  }
}
