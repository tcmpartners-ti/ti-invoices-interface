package com.tcmp.tiapi.ti.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceCancellationFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceCreationResultFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceFinancingFlowStrategy;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TICCIncomingHandlerContextTest {
  @Mock private InvoiceCreationResultFlowStrategy invoiceCreationResultFlowStrategy;
  @Mock private InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;
  @Mock private InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;
  @Mock private InvoiceCancellationFlowStrategy invoiceCancellationFlowStrategy;

  @InjectMocks private TICCIncomingHandlerContext ticcIncomingHandlerContext;

  @Test
  void strategy_shouldHandleInvoiceCreationResults() {
    var strategy = ticcIncomingHandlerContext.strategy("TFINVACK");
    assertEquals(invoiceCreationResultFlowStrategy, strategy);
  }

  // This will change in a bit
  @ParameterizedTest
  @ValueSource(strings = {"TFINVSET", "TFINVSETCU"})
  void strategy_shouldHandleInvoiceSettlementResults(String operation) {
    var strategy = ticcIncomingHandlerContext.strategy(operation);
    assertEquals(invoiceSettlementFlowStrategy, strategy);
  }

  @Test
  void strategy_shouldHandleInvoiceFinancingResults() {
    var strategy = ticcIncomingHandlerContext.strategy("TFBCFCRE");
    assertEquals(invoiceFinancingFlowStrategy, strategy);
  }

  @Test
  void strategy_shouldHandleInvoiceCancellationResults() {
    var strategy = ticcIncomingHandlerContext.strategy("TFINVCANA");
    assertEquals(invoiceCancellationFlowStrategy, strategy);
  }
}
