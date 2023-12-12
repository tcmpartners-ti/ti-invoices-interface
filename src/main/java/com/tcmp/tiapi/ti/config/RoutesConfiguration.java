package com.tcmp.tiapi.ti.config;

import com.tcmp.tiapi.ti.route.FTIOutgoingRouteBuilder;
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

  @Bean
  public FTIOutgoingRouteBuilder ftiOutgoingRouteBuilder(
      JaxbDataFormat jaxbDataFormatServiceRequest) {
    return new FTIOutgoingRouteBuilder(
        jaxbDataFormatServiceRequest,
        new XmlNamespaceFixer(),
        uriFromFtiOutgoing,
        uriToFtiOutgoing);
  }
}
