package com.tcmp.tiapi.invoice.route;

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
public class InvoiceCreatedEventListenerRouteBuilder extends RouteBuilder {
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final int RETRIES_DELAY_IN_MS = 1_000;

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

    from(uriFrom).routeId("invoiceCreationResult")
      .unmarshal(jaxbDataFormat)
      .to("log:body")
      .end();

    from(uriTo).routeId("apiGeeInvoiceCreationNotifier")
      .process(this::sendInvoicePayloadOrThrowException)
      .end();
  }

  private void sendInvoicePayloadOrThrowException(Exchange exchange) {
    ServiceResponse serviceResponse = exchange.getIn().getBody(ServiceResponse.class);
    if (serviceResponse == null) return;
    log.info("ServiceResponse={}", serviceResponse);

    String invoiceNumber = serviceResponse.getResponseHeader().getCorrelationId();
    businessBankingService.sendInvoiceCreationResult(serviceResponse, invoiceNumber);
  }
}
