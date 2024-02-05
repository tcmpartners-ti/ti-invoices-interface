package com.tcmp.tiapi.invoice.strategy.ticc;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.cancel.CancelInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCancellationFlowStrategy implements TICCIncomingStrategy {
  private final OperationalGatewayService operationalGatewayService;

  private final InvoiceRepository invoiceRepository;
  private final CustomerRepository customerRepository;

  @Override
  public void handleServiceRequest(AckServiceRequest<?> request) {
    CancelInvoiceEventMessage message = (CancelInvoiceEventMessage) request.getBody();

    try {
      Customer seller = findSellerByMnemonic(message.getSellerIdentifier());
      InvoiceMaster invoice = findInvoiceByMasterReference(message.getMasterRef());

      notifyInvoiceCancellationStatusToSeller(message, seller, invoice);
    } catch (EntityNotFoundException | OperationalGatewayException e) {
      log.error(e.getMessage());
    }
  }

  private Customer findSellerByMnemonic(String sellerIdentifier) {
    return customerRepository
        .findFirstByIdMnemonic(sellerIdentifier)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    String.format("Could not find seller with mnemonic %s", sellerIdentifier)));
  }

  private InvoiceMaster findInvoiceByMasterReference(String masterReference) {
    return invoiceRepository
        .findByProductMasterMasterReference(masterReference)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    String.format("Could not find invoice with master %s", masterReference)));
  }

  private void notifyInvoiceCancellationStatusToSeller(
      CancelInvoiceEventMessage cancelInvoiceMessage, Customer seller, InvoiceMaster invoice) {

    InvoiceEmailInfo cancelledInvoiceEmailInfo =
        InvoiceEmailInfo.builder()
            .customerMnemonic(cancelInvoiceMessage.getSellerIdentifier())
            .customerEmail(seller.getAddress().getCustomerEmail().trim())
            .customerName(seller.getFullName().trim())
            .date(cancelInvoiceMessage.getCancellationDate())
            .action(InvoiceEmailEvent.CANCELLED.getValue())
            .invoiceNumber(cancelInvoiceMessage.getInvoiceNumber())
            .invoiceCurrency(invoice.getFaceValueCurrencyCode().trim())
            .amount(MonetaryAmountUtils.convertCentsToDollars(invoice.getFaceValueAmount()))
            .build();

    operationalGatewayService.sendNotificationRequest(cancelledInvoiceEmailInfo);
  }
}
