package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.messaging.NamespaceFixerProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class InvoiceRouter extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;

  private final String uriFromCreate;
  private final String uriToCreatePub;
  private final String uriToCreateSub;

  @Override
  public void configure() {
    from(uriFromCreate).routeId("createInvoiceInTI")
      .marshal(jaxbDataFormat).process(new NamespaceFixerProcessor())
      .log("${body}")
      .to(uriToCreatePub)
      .end();

    from(uriToCreateSub).routeId("invoiceCreationResult")
      .log("${body}")
      .end();
  }
}
