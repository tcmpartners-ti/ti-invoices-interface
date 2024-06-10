package com.tcmp.tiapi.invoice.strategy.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.strategy.ticc.InvoiceSettlementFlowStrategy;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.strategy.PaymentResultStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementPaymentResultStrategy implements PaymentResultStrategy {
  private final ObjectMapper objectMapper;

  private final InvoiceRepository invoiceRepository;
  private final CustomerRepository customerRepository;
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

    InvoiceMaster invoice =
        invoiceRepository
            .findByProductMasterMasterReference(message.getMasterRef())
            .orElseThrow(EntityNotFoundException::new);
    Customer seller =
        customerRepository
            .findFirstByIdMnemonic(message.getSellerIdentifier())
            .orElseThrow(EntityNotFoundException::new);

    boolean onlyCreditSucceeded =
        paymentResultResponse.type().equals(PaymentResultResponse.Type.CLIENT_BGL)
            && paymentResultResponse.status().equals(PaymentResultResponse.Status.FAILED);
    if (onlyCreditSucceeded) {
      String errorMessage = "Could not perform bgl to seller transaction";
      invoiceSettlementFlowStrategy
          .notifySettlementStatusExternally(
              PayloadStatus.FAILED, message, invoice, null, errorMessage)
          .onErrorResume(
              error -> invoiceSettlementFlowStrategy.handleError(error, message, invoice))
          .subscribe();

      return;
    }

    ProductMasterExtension invoiceExtension =
        invoiceSettlementFlowStrategy.findMasterExtensionByReference(message.getMasterRef());

    // Credit received, should be all good
    invoiceSettlementFlowStrategy
        .sendEmailToCustomer(InvoiceEmailEvent.PROCESSED, message, seller)
        .then(
            invoiceSettlementFlowStrategy.notifySettlementStatusExternally(
                PayloadStatus.SUCCEEDED,
                message,
                invoice,
                invoiceExtension.getGafOperationId(),
                null))
        .onErrorResume(error -> invoiceSettlementFlowStrategy.handleError(error, message, invoice))
        .subscribe();
  }
}
