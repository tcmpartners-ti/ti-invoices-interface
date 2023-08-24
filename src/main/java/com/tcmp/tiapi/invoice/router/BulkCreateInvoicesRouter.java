package com.tcmp.tiapi.invoice.router;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.NamespaceFixerProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.BindyType;

@RequiredArgsConstructor
public class BulkCreateInvoicesRouter extends RouteBuilder {
  private static final int THREAD_POOL_SIZE_FOR_BULK_OPERATIONS = 5;

  private final JaxbDataFormat jaxbDataFormat;
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final InvoiceMapper invoiceMapper;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    // Desacoplar de Camel el processor
    from(uriFrom).routeId("bulkCreateInvoicesBulkInTI").threads(THREAD_POOL_SIZE_FOR_BULK_OPERATIONS)
      .split(body().tokenize("\n"))
      .filter(simple("${exchangeProperty.CamelSplitIndex} != 0")) // Ignore header
      .unmarshal().bindy(BindyType.Csv, InvoiceCreationRowCSV.class)
      .transform().body(InvoiceCreationRowCSV.class, invoiceMapper::mapCSVRowToFTIMessage)
      .transform().body(CreateInvoiceEventMessage.class, createInvoiceEventMessage -> tiServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.CREATE_INVOICE,
        ReplyFormat.STATUS,
        createInvoiceEventMessage
      ))
      .marshal(jaxbDataFormat).process(new NamespaceFixerProcessor())
      .log("Sending invoice to TI queue...")
      .to(uriTo)
      .end();
  }
}
