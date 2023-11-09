package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.NotificationInvoiceCreationMessage;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
@RequiredArgsConstructor
public class InvoiceNotificationCreationService {
  private final CustomerRepository customerRepository;

  public Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository.findFirstByIdMnemonic(customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find customer with mnemonic " + customerMnemonic));
  }

  public InvoiceEmailInfo buildInvoiceNotificationCreationEmailInfo(
    NotificationInvoiceCreationMessage notificationAckMessage,
    Customer customer,
    InvoiceEmailEvent event
  ) {
    return InvoiceEmailInfo.builder()
      .customerMnemonic(notificationAckMessage.getSellerIdentifier())
      .customerEmail(customer.getAddress().getCustomerEmail().trim())
      .customerName(customer.getFullName().trim())
      .date(notificationAckMessage.getReceivedOn())
      .action(event.getValue())
      .invoiceCurrency(notificationAckMessage.getFaceValueCurrency())
      .invoiceNumber(notificationAckMessage.getInvoiceNumber())
      .amount(getFaceValueAmountFromMessage(notificationAckMessage))
      .build();
  }

  private BigDecimal getFaceValueAmountFromMessage(NotificationInvoiceCreationMessage notificationAckMessage) {
    BigDecimal faceValueAmountInCents = new BigDecimal(notificationAckMessage.getFaceValueAmount());
    return MonetaryAmountUtils.convertCentsToDollars(faceValueAmountInCents);
  }

}
