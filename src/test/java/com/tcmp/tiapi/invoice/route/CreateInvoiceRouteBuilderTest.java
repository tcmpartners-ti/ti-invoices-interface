package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateInvoiceRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:createInvoice";
  private static final String URI_TO = "mock:activeMQ";

  @EndpointInject(URI_FROM)
  ProducerTemplate from;

  @Mock
  private JaxbDataFormat jaxbDataFormat;
  @Mock
  private TIServiceRequestWrapper tiServiceRequestWrapper;
  @Mock
  private XmlNamespaceFixer xmlNamespaceFixer;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new CreateInvoiceRouteBuilder(
      jaxbDataFormat,
      tiServiceRequestWrapper,
      xmlNamespaceFixer,
      URI_FROM,
      URI_TO
    );
  }

  @Test
  void itShouldFollowConversionFlow() throws IOException {
    from.sendBody(CreateInvoiceEventMessage.builder()
      .invoiceNumber("INV123")
      .build());

    verify(tiServiceRequestWrapper)
      .wrapRequest(
        any(TIService.class),
        any(TIOperation.class),
        any(ReplyFormat.class),
        anyString(),
        any(CreateInvoiceEventMessage.class)
      );
    verify(jaxbDataFormat).marshal(any(), any(), any());
    verify(xmlNamespaceFixer).fixNamespaces(anyString());
  }

  @Test
  void itShouldCallWrapperWithSpecificArguments() {
    TIService expectedService = TIService.TRADE_INNOVATION;
    TIOperation expectedOperation = TIOperation.CREATE_INVOICE;
    ReplyFormat expectedReplyFormat = ReplyFormat.STATUS;
    CreateInvoiceEventMessage expectedMessage = CreateInvoiceEventMessage.builder().build();

    from.sendBody(expectedMessage);

    ArgumentCaptor<TIService> serviceArgumentCaptor = ArgumentCaptor.forClass(TIService.class);
    ArgumentCaptor<TIOperation> operationArgumentCaptor = ArgumentCaptor.forClass(TIOperation.class);
    ArgumentCaptor<ReplyFormat> replyFormatArgumentCaptor = ArgumentCaptor.forClass(ReplyFormat.class);
    ArgumentCaptor<String> correlationIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<CreateInvoiceEventMessage> messageArgumentCaptor = ArgumentCaptor.forClass(CreateInvoiceEventMessage.class);

    verify(tiServiceRequestWrapper)
      .wrapRequest(
        serviceArgumentCaptor.capture(),
        operationArgumentCaptor.capture(),
        replyFormatArgumentCaptor.capture(),
        correlationIdArgumentCaptor.capture(),
        messageArgumentCaptor.capture()
      );
    assertThat(serviceArgumentCaptor.getValue()).isEqualTo(expectedService);
    assertThat(operationArgumentCaptor.getValue()).isEqualTo(expectedOperation);
    assertThat(replyFormatArgumentCaptor.getValue()).isEqualTo(expectedReplyFormat);
    assertThat(messageArgumentCaptor.getValue()).isEqualTo(expectedMessage);
  }
}
