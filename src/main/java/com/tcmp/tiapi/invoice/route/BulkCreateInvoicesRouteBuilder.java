package com.tcmp.tiapi.invoice.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCorrelationPayload;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.BindyType;

@RequiredArgsConstructor
public class BulkCreateInvoicesRouteBuilder extends RouteBuilder {
  private static final int THREAD_POOL_SIZE_FOR_BULK_OPERATIONS = 5;

  private final JaxbDataFormat jaxbDataFormat;
  private final ObjectMapper objectMapper;
  private final InvoiceMapper invoiceMapper;
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("bulkCreateInvoicesBulkInTI").threads(THREAD_POOL_SIZE_FOR_BULK_OPERATIONS)
      .split(body().tokenize("\n"))
      .streaming()
      .filter(simple("${exchangeProperty.CamelSplitIndex} != 0")) // Ignore header
      .filter(simple("${body.length} > 0"))
      .unmarshal().bindy(BindyType.Csv, InvoiceCreationRowCSV.class)
      // Map to TI Message
      .transform().body(InvoiceCreationRowCSV.class, (body, headers) -> {
        String batchId = (String) headers.get("batchId");
        return invoiceMapper.mapCSVRowToFTIMessage(body, batchId);
      })
      // Send Correlation header with invoice metadata
      .process(exchange -> {
        CreateInvoiceEventMessage createInvoiceEventMessage = exchange.getIn().getBody(CreateInvoiceEventMessage.class);
        InvoiceCorrelationPayload payload = invoiceMapper.mapFTIMessageToCorrelationPayload(createInvoiceEventMessage);
        String jsonPayload = objectMapper.writeValueAsString(payload);

        exchange.getIn().setHeader("JMSCorrelationID", jsonPayload);
      })
      .transform().body(CreateInvoiceEventMessage.class, createInvoiceEventMessage -> tiServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.CREATE_INVOICE,
        ReplyFormat.STATUS,
        createInvoiceEventMessage.getInvoiceNumber(),
        createInvoiceEventMessage
      ))
      .marshal(jaxbDataFormat)
      .transform().body(String.class, xmlNamespaceFixer::fixNamespaces)
      .log("Sending invoice to TI queue...")
      .to(uriTo)
      .end();
  }
}
