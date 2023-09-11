package com.tcmp.tiapi.invoice.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCorrelationPayload;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.service.OperationalGatewayService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class InvoiceCreatedEventListenerRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;
  private final ObjectMapper objectMapper;
  private final OperationalGatewayService operationalGatewayService;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("invoiceCreationResult")
      .log("${body}")
      .unmarshal(jaxbDataFormat)
      .process(exchange -> {
        ServiceResponse serviceResponse = exchange.getIn().getBody(ServiceResponse.class);
        String correlationJsonPayload = (String) exchange.getIn().getHeaders().get("JMSCorrelationID");
        InvoiceCorrelationPayload invoiceInfo = objectMapper.readValue(correlationJsonPayload, InvoiceCorrelationPayload.class);

        operationalGatewayService.sendInvoiceCreationResult(serviceResponse, invoiceInfo);
      })
      .to(uriTo)
      .end();
  }
}
