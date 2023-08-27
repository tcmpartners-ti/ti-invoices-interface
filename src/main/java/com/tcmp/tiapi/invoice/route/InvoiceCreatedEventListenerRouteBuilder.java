package com.tcmp.tiapi.invoice.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

@RequiredArgsConstructor
public class InvoiceCreatedEventListenerRouteBuilder extends RouteBuilder {

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("invoiceCreationResult")
      .to(uriTo)
      .end();
  }
}
