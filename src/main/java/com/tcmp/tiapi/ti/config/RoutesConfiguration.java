package com.tcmp.tiapi.ti.config;

import com.tcmp.tiapi.ti.handler.FTIReplyIncomingHandler;
import com.tcmp.tiapi.ti.route.FTIOutgoingRouteBuilder;
import com.tcmp.tiapi.ti.route.FTIReplyIncomingRouteBuilder;
import com.tcmp.tiapi.ti.route.processor.XmlNamespaceFixer;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class RoutesConfiguration {
  @Value("${ti.route.fti.out.from}")
  private String uriFromFtiOutgoing;

  @Value("${ti.route.fti.out.to}")
  private String uriToFtiOutgoing;

  @Value("${ti.route.fti.in-reply.from}")
  private String uriFromFtiReplyIncoming;

  @Bean
  public FTIOutgoingRouteBuilder ftiOutgoingRouteBuilder(
      JaxbDataFormat jaxbDataFormatServiceRequest) {
    return new FTIOutgoingRouteBuilder(
        jaxbDataFormatServiceRequest,
        new XmlNamespaceFixer(),
        uriFromFtiOutgoing,
        uriToFtiOutgoing);
  }

  @Bean
  public FTIReplyIncomingRouteBuilder ftiReplyIncomingRouteBuilder(
      JaxbDataFormat jaxbDataFormatServiceResponse,
      FTIReplyIncomingHandler ftiReplyIncomingHandler) {
    return new FTIReplyIncomingRouteBuilder(
        jaxbDataFormatServiceResponse, ftiReplyIncomingHandler, uriFromFtiReplyIncoming);
  }
}
