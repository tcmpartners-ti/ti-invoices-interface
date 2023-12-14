package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceFinancingFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.route.TICCIncomingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TICCIncomingHandler {
  private final InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;
  private final InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;

  private final Map<String, TICCIncomingStrategy> operationToStrategy = new HashMap<>();

  /** Place new strategies here. */
  @PostConstruct
  private void setUp() {
    operationToStrategy.put(
        "TFINVSET", invoiceSettlementFlowStrategy); // Temporal fix for production
    registerOperation(TIOperation.SETTLE_INVOICE, invoiceSettlementFlowStrategy);
    registerOperation(TIOperation.FINANCE_INVOICE_RESULT, invoiceFinancingFlowStrategy);
  }

  private void registerOperation(TIOperation operation, TICCIncomingStrategy strategy) {
    operationToStrategy.put(operation.getValue(), strategy);
  }

  public TICCIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return Optional.ofNullable(operationToStrategy.get(operation))
        .orElseThrow(IllegalArgumentException::new);
  }
}
