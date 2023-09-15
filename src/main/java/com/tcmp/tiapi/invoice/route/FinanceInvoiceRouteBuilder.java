package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.dto.ti.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class FinanceInvoiceRouteBuilder extends RouteBuilder {
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final JaxbDataFormat jaxbDataFormat;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("discountInvoiceInTi")
      .transform().body(FinanceBuyerCentricInvoiceEventMessage.class, this::wrapToServiceRequest)
      .marshal(jaxbDataFormat)
      .transform().body(String.class, xmlNamespaceFixer::fixNamespaces)
      .to(uriTo)
      .end();
  }

  private ServiceRequest<FinanceBuyerCentricInvoiceEventMessage> wrapToServiceRequest(FinanceBuyerCentricInvoiceEventMessage message) {
    return tiServiceRequestWrapper.wrapRequest(
      TIService.TRADE_INNOVATION,
      TIOperation.FINANCE_INVOICE,
      ReplyFormat.STATUS,
      null,
      message
    );
  }
}
