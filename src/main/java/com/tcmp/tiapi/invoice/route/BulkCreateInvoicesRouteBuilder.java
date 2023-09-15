package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.BindyType;

import java.util.Map;

@RequiredArgsConstructor
public class BulkCreateInvoicesRouteBuilder extends RouteBuilder {
  private static final int THREAD_POOL_SIZE_FOR_BULK_OPERATIONS = 5;

  private final JaxbDataFormat jaxbDataFormat;
  private final InvoiceMapper invoiceMapper;
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    ValueBuilder ignoreCsvHeader = simple("${exchangeProperty.CamelSplitIndex} != 0");
    ValueBuilder ignoreEmptyRows = simple("${body.trim()} != ''");

    from(uriFrom).routeId("bulkCreateInvoicesBulkInTI").threads(THREAD_POOL_SIZE_FOR_BULK_OPERATIONS)
      .split(body().tokenize("\n"))
      .streaming()
      .filter(ignoreCsvHeader)
      .filter(ignoreEmptyRows)
      .unmarshal().bindy(BindyType.Csv, InvoiceCreationRowCSV.class)
      .transform().body(InvoiceCreationRowCSV.class, this::mapCsvRowToTIMessage)
      .transform().body(CreateInvoiceEventMessage.class, this::wrapToServiceRequest)
      .marshal(jaxbDataFormat)
      .transform().body(String.class, xmlNamespaceFixer::fixNamespaces)
      .log("Sending invoice to TI queue...")
      .to(uriTo)
      .end();
  }

  private CreateInvoiceEventMessage mapCsvRowToTIMessage(InvoiceCreationRowCSV body, Map<String, Object> headers) {
    String batchId = (String) headers.get("batchId");
    return invoiceMapper.mapCSVRowToFTIMessage(body, batchId);
  }

  private ServiceRequest<CreateInvoiceEventMessage> wrapToServiceRequest(CreateInvoiceEventMessage message) {
    return tiServiceRequestWrapper.wrapRequest(
      TIService.TRADE_INNOVATION,
      TIOperation.CREATE_INVOICE,
      ReplyFormat.STATUS,
      message.getBatchId(),
      message
    );
  }
}
