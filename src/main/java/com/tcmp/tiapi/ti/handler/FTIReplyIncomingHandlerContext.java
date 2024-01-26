package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceCreationStatusNotifierStrategy;
import com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceFinancingStatusNotifierStrategy;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.route.FTIReplyIncomingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FTIReplyIncomingHandlerContext {
  private final InvoiceCreationStatusNotifierStrategy invoiceCreationStatusNotifierStrategy;
  private final InvoiceFinancingStatusNotifierStrategy invoiceFinancingStatusNotifierStrategy;

  public FTIReplyIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return switch (operation) {
      case TIOperation.CREATE_INVOICE_VALUE -> invoiceCreationStatusNotifierStrategy;
      case TIOperation.FINANCE_INVOICE_VALUE -> invoiceFinancingStatusNotifierStrategy;
      default -> throw new IllegalArgumentException("Unhandled operation: " + operation);
    };
  }
}
