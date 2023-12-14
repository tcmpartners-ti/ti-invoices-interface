package com.tcmp.tiapi.ti.handler;

import com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceCreationStatusNotifierStrategy;
import com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceFinancingStatusNotifierStrategy;
import com.tcmp.tiapi.ti.dto.TIOperation;
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
  private final InvoiceCreationStatusNotifierStrategy invoiceCreationStatusNotifierStrategy;
  private final InvoiceFinancingStatusNotifierStrategy invoiceFinancingStatusNotifierStrategy;

  private final Map<String, FTIReplyIncomingStrategy> operationToStrategy = new HashMap<>();

  /**
   * Place new strategies here.
   */
  @PostConstruct
  private void setUp() {
    registerOperation(TIOperation.CREATE_INVOICE, invoiceCreationStatusNotifierStrategy);
    registerOperation(TIOperation.FINANCE_INVOICE, invoiceFinancingStatusNotifierStrategy);
  }

  private void registerOperation(TIOperation operation, FTIReplyIncomingStrategy strategy) {
    operationToStrategy.put(operation.getValue(), strategy);
  }

  public FTIReplyIncomingStrategy strategy(String operation) throws IllegalArgumentException {
    return Optional.ofNullable(operationToStrategy.get(operation))
        .orElseThrow(IllegalArgumentException::new);
  }
}
