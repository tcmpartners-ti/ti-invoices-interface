package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.messaging.NamespaceFixerProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class InvoiceRouter extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;

  private final String uriFromCreate;
  private final String uriToCreate;

  @Override
  public void configure() {
    from(uriFromCreate).routeId("createInvoiceInTI")
      .marshal(jaxbDataFormat).process(new NamespaceFixerProcessor())
      .to(uriToCreate)
      // Respuesta
      .end();
  }
}
