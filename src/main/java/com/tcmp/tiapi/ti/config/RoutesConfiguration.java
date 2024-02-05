package com.tcmp.tiapi.ti.config;

import com.tcmp.tiapi.ti.handler.FTIReplyIncomingHandlerContext;
import com.tcmp.tiapi.ti.handler.ScheduleIncomingHandlerContext;
import com.tcmp.tiapi.ti.handler.TICCIncomingHandlerContext;
import com.tcmp.tiapi.ti.route.fti.FTIOutgoingRouteBuilder;
import com.tcmp.tiapi.ti.route.fti.FTIReplyIncomingRouteBuilder;
import com.tcmp.tiapi.ti.route.processor.XmlNamespaceFixer;
import com.tcmp.tiapi.ti.route.schedule.ScheduleIncomingRouteBuilder;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingRouteBuilder;
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

  @Value("${ti.route.ticc.in.from}")
  private String uriFromTiccIncoming;

  @Value("${ti.route.scheduler.in.from}")
  private String uriFromSchedulerIncoming;

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
      FTIReplyIncomingHandlerContext ftiReplyIncomingHandlerContext) {
    return new FTIReplyIncomingRouteBuilder(
        jaxbDataFormatServiceResponse, ftiReplyIncomingHandlerContext, uriFromFtiReplyIncoming);
  }

  @Bean
  public TICCIncomingRouteBuilder ticcIncomingRouteBuilder(
      JaxbDataFormat jaxbDataFormatAckEventRequest,
      TICCIncomingHandlerContext ticcIncomingHandlerContext) {
    return new TICCIncomingRouteBuilder(
        jaxbDataFormatAckEventRequest, ticcIncomingHandlerContext, uriFromTiccIncoming);
  }

  @Bean
  public ScheduleIncomingRouteBuilder scheduleIncomingRouteBuilder(
      ScheduleIncomingHandlerContext scheduleIncomingHandlerContext) {
    return new ScheduleIncomingRouteBuilder(
        scheduleIncomingHandlerContext, uriFromSchedulerIncoming);
  }
}
