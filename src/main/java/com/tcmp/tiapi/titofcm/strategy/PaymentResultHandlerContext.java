package com.tcmp.tiapi.titofcm.strategy;

import com.tcmp.tiapi.invoice.strategy.payment.FinanceCreditPaymentResultStrategy;
import com.tcmp.tiapi.invoice.strategy.payment.FinanceTaxesPaymentResultStrategy;
import com.tcmp.tiapi.invoice.strategy.payment.SettlementPaymentResultStrategy;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultHandlerContext {
  private final SettlementPaymentResultStrategy settlementPaymentResultStrategy;
  private final FinanceCreditPaymentResultStrategy financeCreditPaymentResultStrategy;
  private final FinanceTaxesPaymentResultStrategy financeTaxesPaymentResultStrategy;

  public PaymentResultStrategy getStrategy(InvoicePaymentCorrelationInfo.InitialEvent event) {
    return switch (event) {
      case SETTLEMENT -> settlementPaymentResultStrategy;
      case BUYER_CENTRIC_FINANCE_0 -> financeCreditPaymentResultStrategy;
      case BUYER_CENTRIC_FINANCE_1 -> financeTaxesPaymentResultStrategy;
    };
  }
}
