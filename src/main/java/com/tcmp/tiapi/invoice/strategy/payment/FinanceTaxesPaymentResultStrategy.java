package com.tcmp.tiapi.invoice.strategy.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.exception.InconsistentInvoiceInformationException;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceFinancingFlowStrategy;
import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.strategy.PaymentResultStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinanceTaxesPaymentResultStrategy implements PaymentResultStrategy {
  private final ObjectMapper objectMapper;

  private final InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;

  @Override
  public void handleResult(
      InvoicePaymentCorrelationInfo invoicePaymentInfo, PaymentResultResponse paymentResult) {
    FinanceAckMessage financeMessage = readMessageFromCorrelationInfo(invoicePaymentInfo);
    invoiceFinancingFlowStrategy.handleTaxesPaymentResult(
        financeMessage, paymentResult, invoicePaymentInfo);
  }

  private FinanceAckMessage readMessageFromCorrelationInfo(InvoicePaymentCorrelationInfo info)
      throws InconsistentInvoiceInformationException {
    try {
      return objectMapper.readValue(info.getEventPayload(), FinanceAckMessage.class);
    } catch (JsonProcessingException e) {
      throw new InconsistentInvoiceInformationException(e.getMessage());
    }
  }
}
