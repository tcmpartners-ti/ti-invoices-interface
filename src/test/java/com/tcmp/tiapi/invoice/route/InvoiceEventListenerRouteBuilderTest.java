package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceEventListenerRouteBuilderTest extends CamelTestSupport {
  private static final int MAX_RETRIES = 2;
  private static final int RETRY_DELAY_IN_MS = 1;
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
      URI_TO,

      MAX_RETRIES,
      RETRY_DELAY_IN_MS
    );
  }

  @Test
  void itShouldSendInvoiceCreationResult() throws IOException {
    String body = buildMockFailedServiceResponse(TIOperation.CREATE_INVOICE);

    when(invoiceEventService.findInvoiceEventInfoByUuid(anyString()))
      .thenReturn(InvoiceEventInfo.builder().build());

    sendBodyToRoute(body);

    verify(jaxbDataFormat).unmarshal(any(), any());
    verify(invoiceEventService).findInvoiceEventInfoByUuid(anyString());
    verify(businessBankingService).sendInvoiceEventResult(
      any(),
      any(ServiceResponse.class),
      any(InvoiceEventInfo.class)
    );
  }

  static Stream<Arguments> provideItShouldHandleInvoiceEventsTestCases() {
    return Stream.of(
      Arguments.of(TIOperation.CREATE_INVOICE, OperationalGatewayProcessCode.INVOICE_CREATION),
      Arguments.of(TIOperation.FINANCE_INVOICE, OperationalGatewayProcessCode.ADVANCE_INVOICE_DISCOUNT)
    );
  }

  @ParameterizedTest
  @MethodSource("provideItShouldHandleInvoiceEventsTestCases")
  void itShouldHandleInvoiceEvents(TIOperation operation, OperationalGatewayProcessCode expectedProcessCode) {
    when(invoiceEventService.findInvoiceEventInfoByUuid(anyString()))
      .thenReturn(InvoiceEventInfo.builder().build());

    String body = buildMockFailedServiceResponse(operation);
    sendBodyToRoute(body);

    verify(businessBankingService).sendInvoiceEventResult(
      eq(expectedProcessCode),
      any(ServiceResponse.class),
      any(InvoiceEventInfo.class)
    );
  }

  @Test
  void itShouldRetryIfRecoverable() {
    int expectedNumberOfAttempts = MAX_RETRIES + 1;
    String body = buildMockFailedServiceResponse(TIOperation.CREATE_INVOICE);

    doThrow(new RecoverableApiGeeRequestException("Error in first attempt.")) // Attempt 1 Retry 0
      .doThrow(new RecoverableApiGeeRequestException("Error in second attempt")) // Attempt 2 Retry 1
      .doNothing() // Attempt 3 Retry 2
      .when(businessBankingService).sendInvoiceEventResult(any(), any(), any());

    sendBodyToRoute(body);

    verify(businessBankingService, times(expectedNumberOfAttempts))
      .sendInvoiceEventResult(any(), any(), any());
  }

  private void sendBodyToRoute(String mockXmlBody) {
    template.sendBody(URI_FROM, mockXmlBody);
    template.sendBody(URI_TO, mockXmlBody);
  }

  private String buildMockFailedServiceResponse(TIOperation operation) {
    return """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <ns2:ServiceResponse xmlns:ns2="urn:control.services.tiplus2.misys.com">
        <ns2:ResponseHeader>
          <ns2:Service>TI</ns2:Service>
          <ns2:Operation>%s</ns2:Operation>
          <ns2:Status>FAILED</ns2:Status>
          <ns2:Details>
            <ns2:Error>Error 1</ns2:Error>
          </ns2:Details>
          <ns2:CorrelationId>Corr123</ns2:CorrelationId>
        </ns2:ResponseHeader>
      </ns2:ServiceResponse>
        """.formatted(operation.getValue());
  }
}
