package com.tcmp.tiapi.ti.route.fti;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.ti.handler.FTIReplyIncomingHandlerContext;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
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
class FTIReplyIncomingRouteBuilderTest extends CamelTestSupport {
  private static final String FAILED_REPLY =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ServiceResponse xmlns=\"urn:control.services.tiplus2.misys.com\" xmlns:ns2=\"urn:messages.service.ti.apps.tiplus2.misys.com\" xmlns:ns4=\"urn:custom.service.ti.apps.tiplus2.misys.com\" xmlns:ns3=\"urn:common.service.ti.apps.tiplus2.misys.com\"><ResponseHeader><Service>TI</Service><Operation>SCFRelationship</Operation><Status>FAILED</Status><Details><Error>Seller not found - 17224665400012</Error><Warning>Warning</Warning><Info>Info</Info></Details></ResponseHeader></ServiceResponse>";

  private static final String URI_FROM = "direct:mockReplyQueue";

  @EndpointInject(URI_FROM)
  private ProducerTemplate from;

  @Mock private FTIReplyIncomingHandlerContext ftiReplyIncomingHandlerContext;

  @Override
  protected RoutesBuilder createRouteBuilder() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ServiceResponse.class);
    JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
    JaxbDataFormat jaxbDataFormatSpy = spy(jaxbDataFormat);

    return new FTIReplyIncomingRouteBuilder(
        jaxbDataFormatSpy, ftiReplyIncomingHandlerContext, URI_FROM);
  }

  @Test
  void itShouldHandleAndPrintErrors() {
    var strategyMock = mock(FTIReplyIncomingStrategy.class);
    when(ftiReplyIncomingHandlerContext.strategy(anyString())).thenReturn(strategyMock);

    from.sendBody(FAILED_REPLY);

    verify(strategyMock).handleServiceResponse(any(ServiceResponse.class));
  }

  @Test
  void itShouldHandleExceptions() {
    var strategyMock = mock(FTIReplyIncomingStrategy.class);

    var exceptionMock = mock(IllegalArgumentException.class);
    when(ftiReplyIncomingHandlerContext.strategy(anyString())).thenThrow(exceptionMock);

    from.sendBody(FAILED_REPLY);

    verifyNoInteractions(strategyMock);
    verify(exceptionMock).getMessage();
  }
}
