package com.tcmp.tiapi.program;

import com.tcmp.tiapi.messaging.NamespaceFixerProcessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

import java.util.UUID;

@RequiredArgsConstructor
public class ProgramRouter extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;

  @Getter
  private final String uriFrom;
  @Getter
  private final String uriTo;

  @Override
  public void configure() {
    from(uriFrom).routeId("createProgramInTI")
      .process(ex -> ex.getIn().setHeader("fileUuid", UUID.randomUUID().toString()))
      .marshal(jaxbDataFormat)
      .process(new NamespaceFixerProcessor())
      .to(uriTo)
      .end()
      .setBody(ex -> ex.getIn().getHeader("fileUuid"))
      .end();
  }
}
