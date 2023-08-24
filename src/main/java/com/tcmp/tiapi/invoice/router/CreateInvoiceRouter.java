package com.tcmp.tiapi.invoice.router;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.NamespaceFixerProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class CreateInvoiceRouter extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;
  private final TIServiceRequestWrapper tiServiceRequestWrapper;

  private final String uriFromCreate;
  private final String uriToCreatePub;
  private final String uriToCreateSub;

  @Override
  public void configure() {
    from(uriFromCreate).routeId("createInvoiceInTI")
      .transform().body(CreateInvoiceEventMessage.class, createInvoiceEventMessage -> tiServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.CREATE_INVOICE,
        ReplyFormat.STATUS,
        createInvoiceEventMessage
      ))
      .marshal(jaxbDataFormat).process(new NamespaceFixerProcessor())
      .to(uriToCreatePub)
      .end();

    from(uriToCreateSub).routeId("invoiceCreationResult")
      .log("${body}")
      .end();
  }
}
