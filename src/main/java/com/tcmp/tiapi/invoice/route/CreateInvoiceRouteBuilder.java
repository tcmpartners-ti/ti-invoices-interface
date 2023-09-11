package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class CreateInvoiceRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("createInvoiceInTI")
      .transform().body(CreateInvoiceEventMessage.class, createInvoiceEventMessage -> tiServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.CREATE_INVOICE,
        ReplyFormat.STATUS,
        createInvoiceEventMessage.getInvoiceNumber(), // CorrelationId
        createInvoiceEventMessage
      ))
      .marshal(jaxbDataFormat)
      .transform().body(String.class, xmlNamespaceFixer::fixNamespaces)
      .to(uriTo)
      .end();
  }
}
