package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.model.InvoiceCreationEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class InvoiceEventListenerRouteBuilder extends RouteBuilder {
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final int RETRIES_DELAY_IN_MS = 1_000;

  private final InvoiceEventService invoiceEventService;
  private final JaxbDataFormat jaxbDataFormat;
  private final BusinessBankingService businessBankingService;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
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
      .process(this::handleServiceResponseOperation)
      .end();
  }

  private void handleServiceResponseOperation(Exchange exchange) {
    ServiceResponse serviceResponse = exchange.getIn().getBody(ServiceResponse.class);
    String operation = serviceResponse.getResponseHeader().getOperation();

    switch (operation) {
      case TIOperation.CREATE_INVOICE_VALUE -> sendInvoiceCreationResult(serviceResponse);
      case TIOperation.FINANCE_INVOICE_VALUE -> sendInvoiceFinancingResult(serviceResponse);
      default -> log.error("Could not handle TI operation: {}", operation);
    }
  }

  private void sendInvoiceCreationResult(ServiceResponse serviceResponse) {
    log.info("InvoiceCreationResponse={}", serviceResponse);
    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();
    InvoiceCreationEventInfo invoice = invoiceEventService.findInvoiceByUuid(invoiceUuidFromCorrelationId);

    businessBankingService.sendInvoiceCreationResult(serviceResponse, invoice);
    invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
  }

  private void sendInvoiceFinancingResult(ServiceResponse serviceResponse) {
    log.info("InvoiceFinancingResponse={}", serviceResponse);
  }
}
