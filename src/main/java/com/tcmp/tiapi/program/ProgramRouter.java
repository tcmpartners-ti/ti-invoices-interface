package com.tcmp.tiapi.program;

import com.tcmp.tiapi.messaging.NamespaceFixerProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramRouter extends RouteBuilder {
    public static final String DIRECT_CREATE_PROGRAM = "direct:createProgramInTI";

    private final JaxbDataFormat jaxbDataFormat;

    @Override
    public void configure() {
        from(DIRECT_CREATE_PROGRAM).routeId("createProgramInTI")
             // Si esta lógica se repite debería encapsularla en un Processor.
            .process(ex -> ex.getIn().setHeader("fileUuid", UUID.randomUUID().toString()))
            .marshal(jaxbDataFormat)
            .process(new NamespaceFixerProcessor())

            .multicast()
                .to("file:messages-out/programs?fileName=${headers.fileUuid}.xml")
                .to("mock:invoices")

            .end()
            .setBody(ex -> ex.getIn().getHeader("fileUuid"))
            .end();
    }
}
