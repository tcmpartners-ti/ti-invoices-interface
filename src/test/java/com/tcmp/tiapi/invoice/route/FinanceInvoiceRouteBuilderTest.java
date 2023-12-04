package com.tcmp.tiapi.invoice.route;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.InvoiceNumbers;
import com.tcmp.tiapi.invoice.dto.ti.finance.InvoiceNumbersContainer;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import java.io.IOException;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinanceInvoiceRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:financeInvoiceInTi";
  private static final String URI_TO = "mock:activeMQ";

  @EndpointInject(URI_FROM) ProducerTemplate from;

  @Mock private InvoiceEventService invoiceEventService;
  @Mock private TIServiceRequestWrapper tiServiceRequestWrapper;
  @Mock private JaxbDataFormat jaxbDataFormat;
  @Mock private XmlNamespaceFixer xmlNamespaceFixer;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new FinanceInvoiceRouteBuilder(
      invoiceEventService,
      tiServiceRequestWrapper,
      jaxbDataFormat,
      xmlNamespaceFixer,

      URI_FROM,
      URI_TO
    );
  }

  @Test
  void itShouldFollowConversionFlow() throws IOException {
    from.sendBody(FinanceBuyerCentricInvoiceEventMessage.builder()
      .invoiceNumbersContainer(
        InvoiceNumbersContainer.builder()
          .invoiceNumbers(List.of(InvoiceNumbers.builder()
              .invoiceNumber("Invoice123")
            .build())
          ).build())
      .build());

    verify(tiServiceRequestWrapper)
      .wrapRequest(
        eq(TIService.TRADE_INNOVATION),
        eq(TIOperation.FINANCE_INVOICE),
        eq(ReplyFormat.STATUS),
        any(),
        any(FinanceBuyerCentricInvoiceEventMessage.class)
      );
    verify(jaxbDataFormat).marshal(any(), any(), any());
    verify(xmlNamespaceFixer).fixNamespaces(anyString());
  }
}
