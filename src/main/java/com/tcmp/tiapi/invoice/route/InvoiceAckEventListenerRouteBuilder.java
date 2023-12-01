package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.support.builder.Namespaces;

@RequiredArgsConstructor
public class InvoiceAckEventListenerRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormatAckEventRequest;

  private final String uriFrom;
  private final String toFinanceFlow;
  private final String toSettleFlow;

  @Override
  public void configure() {
    Namespaces ns = new Namespaces("ns2", TINamespace.CONTROL);
    ValueBuilder operationXpath = xpath("//ns2:ServiceRequest/ns2:RequestHeader/ns2:Operation", String.class, ns);

    // For now, don't handle errors

    from(uriFrom).routeId("invoiceAckEventResult")
      .unmarshal(jaxbDataFormatAckEventRequest)
      .choice()
        .when(operationXpath.isEqualTo(TIOperation.DUE_INVOICE_VALUE))
          .to(toSettleFlow)

        .when(operationXpath.isEqualTo(TIOperation.FINANCE_ACK_INVOICE_VALUE))
          .to(toFinanceFlow)

        .otherwise()
          .process().body(AckServiceRequest.class, this::logUnhandledOperation)
        .endChoice()
      .endChoice()
      .end();
  }

  private void logUnhandledOperation(AckServiceRequest<?> request) {
    if (request != null && request.getHeader() != null) {
      String operation = request.getHeader().getOperation();
      log.error("[Unhandled Operation]: {}", operation);
    } else {
      log.error("[Unhandled Operation]: Received null or invalid AckServiceRequest");
    }
  }
}
