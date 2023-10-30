package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.model.dataformat.BindyType;

import java.util.Map;

@RequiredArgsConstructor
public class BulkCreateInvoicesRouteBuilder extends RouteBuilder {
  private static final int THREAD_POOL_SIZE_FOR_BULK_OPERATIONS = 5;

  private final InvoiceMapper invoiceMapper;

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
      .to(uriTo)
      .end();
  }

  private CreateInvoiceEventMessage mapCsvRowToTIMessage(InvoiceCreationRowCSV body, Map<String, Object> headers) {
    String batchId = (String) headers.get("batchId");
    return invoiceMapper.mapCSVRowToFTIMessage(body, batchId);
  }
}
