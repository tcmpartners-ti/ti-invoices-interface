package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceCreationNotifierStrategy;
import com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceFinancingNotifierStrategy;
import com.tcmp.tiapi.ti.model.TIOperation;
import com.tcmp.tiapi.ti.route.FTIReplyIncomingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FTIReplyIncomingHandler {
  private final InvoiceCreationNotifierStrategy invoiceCreationNotifierStrategy;
  private final InvoiceFinancingNotifierStrategy invoiceFinancingNotifierStrategy;

  private final Map<String, FTIReplyIncomingStrategy> operationToStrategy = new HashMap<>();

  /**
   * Place new strategies here.
   */
  @PostConstruct
  private void setUp() {
    registerOperation(TIOperation.CREATE_INVOICE, invoiceCreationNotifierStrategy);
    registerOperation(TIOperation.FINANCE_INVOICE, invoiceFinancingNotifierStrategy);
  }

  private void registerOperation(TIOperation operation, FTIReplyIncomingStrategy strategy) {
    operationToStrategy.put(operation.getValue(), strategy);
  }

  public FTIReplyIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return Optional.ofNullable(operationToStrategy.get(operation))
        .orElseThrow(IllegalArgumentException::new);
  }
}
