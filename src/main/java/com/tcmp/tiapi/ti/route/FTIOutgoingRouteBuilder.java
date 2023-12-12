package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.route.processor.XmlNamespaceFixer;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/**
 * This route is in charge of the following: - Marshalling the xml classes. - Fix the namespaces. -
 * Send the message to the queue.
 */
@RequiredArgsConstructor
public class FTIOutgoingRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormatServiceRequest;
  private final XmlNamespaceFixer xmlNamespaceFixer;

  private final String uriFrom;
  private final String toFtiOutgoingQueue;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("ftiOutgoingQueue")
        .marshal(jaxbDataFormatServiceRequest)
        .transform()
        .body(String.class, xmlNamespaceFixer::fixNamespaces)
        .to(toFtiOutgoingQueue);
  }
}
