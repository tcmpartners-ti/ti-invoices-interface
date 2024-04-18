package com.tcmp.tiapi.titofcm.strategy;

import com.tcmp.tiapi.invoice.strategy.payment.SettlementPaymentResultStrategy;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultHandlerContext {
  private final SettlementPaymentResultStrategy settlementPaymentResultStrategy;

  public PaymentResultStrategy getStrategy(InvoicePaymentCorrelationInfo.InitialEvent event) {
    return switch (event) {
      case SETTLEMENT -> settlementPaymentResultStrategy;
      default -> throw new IllegalArgumentException("Unhandled event");
    };
  }
}
