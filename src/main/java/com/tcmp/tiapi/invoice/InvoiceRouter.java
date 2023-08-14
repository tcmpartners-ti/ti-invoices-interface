package com.tcmp.tiapi.invoice;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

@RequiredArgsConstructor
public class InvoiceRouter extends RouteBuilder {
  private final String uriFromCreate;
  private final String uriToCreate;

  @Override
  public void configure() {
    from(uriFromCreate).routeId("createInvoiceInTI")
      .marshal().jaxb()
      .to(uriToCreate)
      // Respuesta
      .end();
  }
}
