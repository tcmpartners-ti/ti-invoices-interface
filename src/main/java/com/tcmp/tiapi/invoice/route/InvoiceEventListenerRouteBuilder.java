package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.support.builder.Namespaces;

@RequiredArgsConstructor
public class InvoiceEventListenerRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;
  private final InvoiceEventService invoiceEventService;
  private final BusinessBankingService businessBankingService;

  private final String uriFrom;
  private final String uriTo;

  private final int maxRetries;
  private final int retryDelayInMs;

  @Override
  public void configure() {
    Namespaces ns = new Namespaces("ns2", TINamespace.CONTROL);
    ValueBuilder operationXpath = xpath("//ns2:ServiceResponse/ns2:ResponseHeader/ns2:Operation", String.class, ns);

    onException(UnrecoverableApiGeeRequestException.class)
      .log(LoggingLevel.ERROR, "Could not notify invoice creation.")
      .handled(true)
      .end();

    onException(RecoverableApiGeeRequestException.class)
      .log("Retrying to notify invoice event...")
      .maximumRedeliveries(maxRetries)
      .redeliveryDelay(retryDelayInMs)
      .to(uriTo)
      .handled(true)
      .end();

    from(uriFrom).routeId("invoiceEventResult")
      .unmarshal(jaxbDataFormat)
      .to(uriTo)
      .end();

    from(uriTo).routeId("apiGeeInvoiceCreationNotifier")
      .choice()
        .when(operationXpath.isEqualTo(TIOperation.CREATE_INVOICE_VALUE))
          .process().body(
            ServiceResponse.class,
            body -> sendInvoiceEventResult(OperationalGatewayProcessCode.INVOICE_CREATION, body)
          )
          .log("Invoice creation event notified.")
        .when(operationXpath.isEqualTo(TIOperation.FINANCE_INVOICE_VALUE))
          .process().body(
            ServiceResponse.class,
            body -> sendInvoiceEventResult(OperationalGatewayProcessCode.ADVANCE_INVOICE_DISCOUNT, body)
          )
          .log("Invoice financing event notified.")
        .otherwise()
          .log(LoggingLevel.ERROR, "Unknown Trade Innovation operation.")
      .endChoice()
      .end();
  }

  private void sendInvoiceEventResult(OperationalGatewayProcessCode processCode, ServiceResponse serviceResponse) {
    if (serviceResponse == null) {
      throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    }

    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();

    try {
      InvoiceEventInfo invoice = invoiceEventService.findInvoiceEventInfoByUuid(invoiceUuidFromCorrelationId);

      businessBankingService.sendInvoiceEventResult(processCode, serviceResponse, invoice);
      invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
    } catch (EntityNotFoundException e) {
      throw new UnrecoverableApiGeeRequestException(e.getMessage());
    }
  }
}
