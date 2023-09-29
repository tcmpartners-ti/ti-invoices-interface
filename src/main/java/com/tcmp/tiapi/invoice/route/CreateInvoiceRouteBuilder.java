package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

import java.util.UUID;

@RequiredArgsConstructor
public class CreateInvoiceRouteBuilder extends RouteBuilder {
  private final InvoiceEventService invoiceEventService;
  private final JaxbDataFormat jaxbDataFormat;
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("createInvoiceInTI")
      .transform().body(CreateInvoiceEventMessage.class, this::wrapToServiceRequest)
      .process(this::saveInvoiceCreationInfo)
      .marshal(jaxbDataFormat)
      .transform().body(String.class, xmlNamespaceFixer::fixNamespaces)
      .to(uriTo)
      .end();
  }

  private ServiceRequest<CreateInvoiceEventMessage> wrapToServiceRequest(CreateInvoiceEventMessage message) {
    String invoiceCreationInfoUuid = UUID.randomUUID().toString();

    return tiServiceRequestWrapper.wrapRequest(
      TIService.TRADE_INNOVATION,
      TIOperation.CREATE_INVOICE,
      ReplyFormat.STATUS,
      invoiceCreationInfoUuid,
      message
    );
  }

  private void saveInvoiceCreationInfo(Exchange exchange) {
    ServiceRequest<?> createInvoiceServiceRequest = exchange.getIn().getBody(ServiceRequest.class);

    if (createInvoiceServiceRequest == null) return;

    invoiceEventService.saveInvoiceInfoFromCreationMessage(
      createInvoiceServiceRequest.getHeader().getCorrelationId(),
      (CreateInvoiceEventMessage) createInvoiceServiceRequest.getBody()
    );
  }
}
