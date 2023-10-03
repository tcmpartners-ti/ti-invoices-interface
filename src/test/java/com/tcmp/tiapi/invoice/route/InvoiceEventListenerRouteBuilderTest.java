package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.model.InvoiceCreationEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceEventListenerRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:mockActiveMqQueue";
  private static final String URI_TO = "direct:sendToApiGee";

  @Mock private JaxbDataFormat jaxbDataFormat;
  @Mock private InvoiceEventService invoiceEventService;
  @Mock private BusinessBankingService businessBankingService;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new InvoiceEventListenerRouteBuilder(
      jaxbDataFormat,
      invoiceEventService,
      businessBankingService,

      URI_FROM,
      URI_TO
    );
  }

  @Test
  void itShouldSendInvoiceCreationResult() throws IOException {
    String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:ServiceResponse xmlns:ns2=\"urn:control.services.tiplus2.misys.com\"><ns2:ResponseHeader><ns2:Service>TI</ns2:Service><ns2:Operation>TFINVNEW</ns2:Operation><ns2:Status>FAILED</ns2:Status><ns2:Details><ns2:Error>Duplicate invoice number - FAC-DDE-0018</ns2:Error></ns2:Details><ns2:CorrelationId>LOT_DDE_0005</ns2:CorrelationId></ns2:ResponseHeader></ns2:ServiceResponse>";

    when(invoiceEventService.findInvoiceByUuid(anyString()))
      .thenReturn(InvoiceCreationEventInfo.builder().build());

    template.sendBody(URI_FROM, body);
    template.sendBody(URI_TO, body);

    verify(jaxbDataFormat).unmarshal(any(), any());
    verify(invoiceEventService).findInvoiceByUuid(anyString());
    verify(businessBankingService).sendInvoiceCreationResult(any(ServiceResponse.class), any(InvoiceCreationEventInfo.class));
  }
}
