package com.tcmp.tiapi.titofcm.strategy;

import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;

public interface PaymentResultStrategy {
  void handleResult(InvoicePaymentCorrelationInfo info, PaymentResultResponse paymentResultResponse);
}
