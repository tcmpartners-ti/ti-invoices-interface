package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceCancellationFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceCreationResultFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceFinancingFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.route.TICCIncomingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TICCIncomingHandlerContext {
  private final InvoiceCreationResultFlowStrategy invoiceCreationResultFlowStrategy;
  private final InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;
  private final InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;
  private final InvoiceCancellationFlowStrategy invoiceCancellationFlowStrategy;

  public TICCIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return switch (operation) {
      case TIOperation.CREATE_INVOICE_RESULT_VALUE -> invoiceCreationResultFlowStrategy;
        // Workaround for production
      case "TFINVSET", TIOperation.SETTLE_INVOICE_RESULT_VALUE -> invoiceSettlementFlowStrategy;
      case TIOperation.FINANCE_INVOICE_RESULT_VALUE -> invoiceFinancingFlowStrategy;
      case TIOperation.CANCEL_INVOICE_RESULT_VALUE -> invoiceCancellationFlowStrategy;
      default -> throw new IllegalArgumentException("Unhandled operation: " + operation);
    };
  }
}
