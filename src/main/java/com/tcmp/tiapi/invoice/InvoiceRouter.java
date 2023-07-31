package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.messaging.NamespaceFixerProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.BindyType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceRouter extends RouteBuilder {
    public static final String DIRECT_CREATE_INVOICE = "direct:createInvoiceInTI";
    public static final String DIRECT_CREATE_INVOICES_BULK = "direct:createInvoicesBulkInTI";

    private static final String PUBLISH_TO_INVOICES_QUEUE = "activemq:queue:invoices?connectionFactory=#jmsConnectionFactory";

    private static final int THREAD_POOL_SIZE_FOR_BULK_OPERATIONS = 5;

    private final InvoiceMapper invoiceMapper;
    private final JaxbDataFormat jaxbDataFormat;

    @Override
    public void configure() {
        from(DIRECT_CREATE_INVOICE).routeId("createInvoiceInTI")
            .process(ex -> ex.getIn().setHeader("fileUuid", UUID.randomUUID().toString()))
            .marshal(jaxbDataFormat)
            .process(new NamespaceFixerProcessor())
            .to(PUBLISH_TO_INVOICES_QUEUE)
            .end()
            .setBody(ex -> ex.getIn().getHeader("fileUuid"))
            .end();

        // Current process: Csv -> Csv Object -> Map -> Message Object
        // I should try: Csv -> Map -> Message Object
        from(DIRECT_CREATE_INVOICES_BULK).routeId("createInvoicesBulkInTI").threads(THREAD_POOL_SIZE_FOR_BULK_OPERATIONS)
            .split(body().tokenize("\n"))
                .filter(simple("${exchangeProperty.CamelSplitIndex} != 0")) // Ignore header
                .unmarshal().bindy(BindyType.Csv, InvoiceCreationRowCSV.class)
                .log("Sending invoice to TI queue...")
                .process(ex -> {
                    InvoiceCreationRowCSV csvInvoice = ex.getIn().getBody(InvoiceCreationRowCSV.class);
                    ex.getIn().setBody(invoiceMapper.mapCSVRowToFTIMessage(csvInvoice));
                })
                .to(DIRECT_CREATE_INVOICE)
            .end();
    }
}
