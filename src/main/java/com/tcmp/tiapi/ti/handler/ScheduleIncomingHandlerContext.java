package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.schedule.ScheduleInvoiceSettlementMessageStrategy;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.route.schedule.ScheduleIncomingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleIncomingHandlerContext {
  private final ScheduleInvoiceSettlementMessageStrategy scheduleInvoiceSettlementMessageStrategy;

  public ScheduleIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return switch (operation) {
      case TIOperation.SETTLE_INVOICE_RESULT_VALUE -> scheduleInvoiceSettlementMessageStrategy;
      default -> throw new IllegalArgumentException("Unhandled operation: " + operation);
    };
  }
}
