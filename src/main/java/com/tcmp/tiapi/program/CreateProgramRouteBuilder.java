package com.tcmp.tiapi.program;

import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

@RequiredArgsConstructor
public class CreateProgramRouteBuilder extends RouteBuilder {
  private final TIServiceRequestWrapper tiServiceRequestWrapper;
  private final JaxbDataFormat jaxbDataFormat;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("createProgramInTI")
      .transform().body(SCFProgrammeMessage.class, createProgramMessage -> tiServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.SCF_PROGRAMME,
        ReplyFormat.STATUS,
        null,
        createProgramMessage
      ))
      .marshal(jaxbDataFormat)
      .transform().body(String.class, xmlNamespaceFixer::fixNamespaces)
      .to(uriTo)
      .end();
  }
}
