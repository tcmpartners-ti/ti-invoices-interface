package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.NotificationInvoiceCreationMessage;
import com.tcmp.tiapi.invoice.service.InvoiceNotificationCreationService;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.support.builder.Namespaces;

@RequiredArgsConstructor
public class NotificationInvoiceCreationRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;
  private final InvoiceNotificationCreationService invoiceNotificationCreationService;
  private final OperationalGatewayService operationalGatewayService;

  private final String uriFrom;

  @Override
  public void configure() {
    Namespaces ns = new Namespaces("ns2", TINamespace.CONTROL);
    ValueBuilder operationXpath =
        xpath("//ns2:ServiceRequest/ns2:RequestHeader/ns2:Operation", String.class, ns);

    from(uriFrom)
        .routeId("NotificationInvoiceCreationResult")
        .unmarshal(jaxbDataFormat)
        .choice()
        .when(operationXpath.isEqualTo(TIOperation.NOTIFICATION_CREATION_ACK_INVOICE_VALUE))
        .log("Started invoice notification creation flow.")
        .process()
        .body(AckServiceRequest.class, this::startInvoiceNotificationFlow)
        .log("Invoice notification creation flow completed successfully.")
        .endChoice()
        .otherwise()
        .log(LoggingLevel.ERROR, "Unknown Trade Innovation operation.")
        .to("log:body")
        .endChoice()
        .endChoice()
        .end();
  }

  private void startInvoiceNotificationFlow(
      AckServiceRequest<NotificationInvoiceCreationMessage> serviceRequest) {
    if (serviceRequest == null)
      throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    NotificationInvoiceCreationMessage invoiceNotificationCreationMessage =
        serviceRequest.getBody();

    Customer seller =
        invoiceNotificationCreationService.findCustomerByMnemonic(
            invoiceNotificationCreationMessage.getSellerIdentifier());

    operationalGatewayService.sendNotificationRequest(
        invoiceNotificationCreationService.buildInvoiceNotificationCreationEmailInfo(
            invoiceNotificationCreationMessage, seller, InvoiceEmailEvent.POSTED));
  }
}
