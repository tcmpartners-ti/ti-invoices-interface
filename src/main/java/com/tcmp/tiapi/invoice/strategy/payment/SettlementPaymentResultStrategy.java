package com.tcmp.tiapi.invoice.strategy.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.strategy.PaymentResultStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementPaymentResultStrategy implements PaymentResultStrategy {
  private final ObjectMapper objectMapper;

  private final InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;

  @Override
  public void handleResult(
      InvoicePaymentCorrelationInfo invoicePaymentCorrelationInfo,
      PaymentResultResponse paymentResultResponse) {
    InvoiceSettlementEventMessage message;
    try {
      String eventPayload = invoicePaymentCorrelationInfo.getEventPayload();
      message = objectMapper.readValue(eventPayload, InvoiceSettlementEventMessage.class);
    } catch (JsonProcessingException e) {
      throw new SinglePaymentException(e.getMessage());
    }

    invoiceSettlementFlowStrategy
        .handleTransactionPaymentResult(message, paymentResultResponse)
        .subscribe();
  }
}
