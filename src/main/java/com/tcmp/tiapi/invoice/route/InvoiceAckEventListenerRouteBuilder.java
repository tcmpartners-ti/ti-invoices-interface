package com.tcmp.tiapi.invoice.route;

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

public class InvoiceAckEventListenerRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;
  private final BusinessBankingService businessBankingService;

  private final String uriFrom;
  private final String uriTo;

  private final int maxRetries;
  private final int retryDelayInMs;

  @Override
  public void configure() {
    Namespaces ns = new Namespaces("ns2", TINamespace.CONTROL);
    ValueBuilder operationXpath = xpath("//ns2:ServiceRequest/ns2:RequestHeader/ns2:Operation", String.class, ns);

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


   from(uriFrom).routeId("invoiceAckEventResult")
     .unmarshal(jaxbDataFormat)
     .to(uriTo)
     .log(" salida ${body}")
     .log("fin paso 1")
     .end();

    from(uriTo).routeId("apiGeeInvoiceAckNotifier")
      .log("paso segundo")
      .choice()
      .when(operationXpath.isEqualTo(TIOperation.DUE_INVOICE_VALUE))
      .process().body(
        ServiceResponse.class,
        body -> sendInvoiceAckEventResult(OperationalGatewayProcessCode.INVOICE_DUE_DATE_REACHED, body)
      )
      .log("Invoice creation event notified.")
      .otherwise()
      .log(LoggingLevel.ERROR, "Unknown 2Trade Innovation operation.")
      .endChoice()
      .end();


  }

  private void sendInvoiceAckEventResult(OperationalGatewayProcessCode processCode, ServiceResponse serviceResponse) {
    if (serviceResponse == null) {
      throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    }

    try {
          businessBankingService.sendInvoiceAckEventResult(processCode, serviceResponse);
    } catch (EntityNotFoundException e) {
      throw new UnrecoverableApiGeeRequestException(e.getMessage());
    }
  }


}
