package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.ticc.*;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TICCIncomingHandlerContext {
  private final InvoiceCreationResultFlowStrategy invoiceCreationResultFlowStrategy;
  private final InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;
  private final InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;
  private final InvoiceCancellationFlowStrategy invoiceCancellationFlowStrategy;
  private final SellerInvoiceFinancingFlowStrategy sellerInvoiceFinancingFlowStrategy;

  public TICCIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return switch (operation) {
      case TIOperation.CREATE_INVOICE_RESULT_VALUE -> invoiceCreationResultFlowStrategy;
        // Workaround for production
      case "TFINVSET", TIOperation.SETTLE_INVOICE_RESULT_VALUE -> invoiceSettlementFlowStrategy;
      case TIOperation.FINANCE_INVOICE_RESULT_VALUE -> invoiceFinancingFlowStrategy;
      case TIOperation.CANCEL_INVOICE_RESULT_VALUE -> invoiceCancellationFlowStrategy;
      case TIOperation.FINANCE_SELLER_INVOICE_RESULT_VALUE -> sellerInvoiceFinancingFlowStrategy;
      default -> throw new IllegalArgumentException("Unhandled operation: " + operation);
    };
  }
}
