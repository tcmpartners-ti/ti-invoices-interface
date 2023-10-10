package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.support.builder.Namespaces;

@RequiredArgsConstructor
public class InvoiceEventListenerRouteBuilder extends RouteBuilder {
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final int RETRIES_DELAY_IN_MS = 1_000;

  private final JaxbDataFormat jaxbDataFormat;
  private final InvoiceEventService invoiceEventService;
  private final BusinessBankingService businessBankingService;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    Namespaces ns = new Namespaces("ns2", "urn:control.services.tiplus2.misys.com");
    ValueBuilder operation = xpath("//ns2:ServiceResponse/ns2:ResponseHeader/ns2:Operation", String.class, ns);

    onException(UnrecoverableApiGeeRequestException.class)
      .log(LoggingLevel.ERROR, "Could not notify invoice creation.")
      .end();

    onException(RecoverableApiGeeRequestException.class)
      .maximumRedeliveries(MAX_RETRY_ATTEMPTS)
      .redeliveryDelay(RETRIES_DELAY_IN_MS)
      .to(uriTo)
      .end();

    from(uriFrom).routeId("invoiceEventResult")
      .unmarshal(jaxbDataFormat)
      .to(uriTo)
      .end();

    from(uriTo).routeId("apiGeeInvoiceCreationNotifier")
      .choice()
        .when(operation.isEqualTo(TIOperation.CREATE_INVOICE_VALUE))
          .process().body(
            ServiceResponse.class,
            body -> sendInvoiceEventResult(body, OperationalGatewayProcessCode.INVOICE_CREATION)
          )
          .log("Invoice creation event notified.")
        .when(operation.isEqualTo(TIOperation.FINANCE_INVOICE_VALUE))
          .process().body(
            ServiceResponse.class,
            body -> sendInvoiceEventResult(body, OperationalGatewayProcessCode.ADVANCE_INVOICE_DISCOUNT)
          )
          .log("Invoice financing event notified.")
        .otherwise()
          .log(LoggingLevel.ERROR, "Unknown Trade Innovation operation.")
      .endChoice()
      .end();
  }

  private void sendInvoiceEventResult(ServiceResponse serviceResponse, OperationalGatewayProcessCode processCode) {
    if (serviceResponse == null) {
      throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    }

    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();
    InvoiceEventInfo invoice = invoiceEventService.findInvoiceEventInfoByUuid(invoiceUuidFromCorrelationId);

    businessBankingService.sendInvoiceEventResult(processCode, serviceResponse, invoice);
    invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
  }
}
