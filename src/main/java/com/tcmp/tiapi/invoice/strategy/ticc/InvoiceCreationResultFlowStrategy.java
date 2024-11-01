package com.tcmp.tiapi.invoice.strategy.ticc;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.creation.InvoiceCreationResultMessage;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCreationResultFlowStrategy implements TICCIncomingStrategy {
  private final OperationalGatewayService operationalGatewayService;

  private final CustomerRepository customerRepository;

  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    InvoiceCreationResultMessage invoiceCreationResultMessage =
        (InvoiceCreationResultMessage) serviceRequest.getBody();

    try {
      Customer seller = findCustomerByMnemonic(invoiceCreationResultMessage.getSellerIdentifier());

      operationalGatewayService.sendNotificationRequest(
          buildInvoiceCreationEmailInfo(invoiceCreationResultMessage, seller));
    } catch (EntityNotFoundException | OperationalGatewayException e) {
      log.error(e.getMessage());
    }
  }

  private Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository
        .findFirstByIdMnemonic(customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find customer with mnemonic " + customerMnemonic));
  }

  private InvoiceEmailInfo buildInvoiceCreationEmailInfo(
      InvoiceCreationResultMessage invoiceCreationResultMessage, Customer customer) {
    return InvoiceEmailInfo.builder()
        .customerMnemonic(invoiceCreationResultMessage.getSellerIdentifier())
        .customerEmail(customer.getAddress().getCustomerEmail().trim())
        .customerName(customer.getFullName().trim())
        .date(invoiceCreationResultMessage.getReceivedOn())
        .action(InvoiceEmailEvent.POSTED.getValue())
        .invoiceNumber(invoiceCreationResultMessage.getInvoiceNumber())
        .invoiceCurrency(invoiceCreationResultMessage.getFaceValueCurrency())
        .amount(getFaceValueAmountFromMessage(invoiceCreationResultMessage))
        .build();
  }

  private BigDecimal getFaceValueAmountFromMessage(
      InvoiceCreationResultMessage notificationAckMessage) {
    BigDecimal faceValueAmountInCents = new BigDecimal(notificationAckMessage.getFaceValueAmount());
    return MonetaryAmountUtils.convertCentsToDollars(faceValueAmountInCents);
  }
}
