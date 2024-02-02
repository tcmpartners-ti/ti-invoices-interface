package com.tcmp.tiapi.ti.route.schedule;

import com.tcmp.tiapi.ti.handler.ScheduleIncomingHandlerContext;
import com.tcmp.tiapi.ti.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

/** This route handle messages that require a scheduled sending of messages */
@RequiredArgsConstructor
public class ScheduleIncomingRouteBuilder extends RouteBuilder {
  private final ScheduleIncomingHandlerContext scheduleIncomingHandlerContext;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("scheduleIncomingQueue")
        .threads(2, 5)
        .process()
        .body(String.class, this::handleIncomingMessage)
        .end();
  }

  private void handleIncomingMessage(String message) {
    String operation = MessageUtils.extractFieldFromMessage("Operation", message);

    try {
      ScheduleIncomingStrategy strategy = scheduleIncomingHandlerContext.strategy(operation);
      strategy.handleIncomingMessage(message);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
