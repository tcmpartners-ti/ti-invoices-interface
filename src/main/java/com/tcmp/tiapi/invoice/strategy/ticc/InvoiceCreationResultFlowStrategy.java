package com.tcmp.tiapi.invoice.strategy.ticc;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.request.InvoiceContextDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceNumberDTO;
import com.tcmp.tiapi.invoice.dto.ti.creation.InvoiceCreationResultMessage;
import com.tcmp.tiapi.invoice.service.InvoiceService;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCreationResultFlowStrategy implements TICCIncomingStrategy {
  private final OperationalGatewayService operationalGatewayService;
  private final InvoiceService invoiceService;
  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;
  private final CurrencyAmountMapper currencyAmountMapper;

  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    InvoiceCreationResultMessage invoiceCreationResultMessage =
            (InvoiceCreationResultMessage) serviceRequest.getBody();

    String buyerIdentifier = invoiceCreationResultMessage.getBuyerIdentifier();
    log.info("BUYER captado " +buyerIdentifier);


    try {
      Customer seller = findCustomerByMnemonic(invoiceCreationResultMessage.getSellerIdentifier());

      operationalGatewayService.sendNotificationRequest(
              buildInvoiceCreationEmailInfo(invoiceCreationResultMessage, seller));
    } catch (EntityNotFoundException | OperationalGatewayException e) {
      log.error(e.getMessage());
    }

    if (buyerIdentifier.equals("1791170032001")) {
      log.info("Factura de Avon detectada. Iniciando proceso de financiación automático.");
      invoiceService.financeInvoice(buildInvoiceFinancingDTO(invoiceCreationResultMessage));
    }
  }

  public InvoiceFinancingDTO buildInvoiceFinancingDTO(
          InvoiceCreationResultMessage invoiceCreationResultMessage) {

    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Asumiendo el formato de entrada
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    String maturityDateFormatted = LocalDate.parse(invoiceCreationResultMessage.getSettlementDate(), inputFormatter)
            .format(outputFormatter);

    String financeDateFormatted = LocalDate.parse(invoiceCreationResultMessage.getIssueDate(), inputFormatter)
            .format(outputFormatter);

    CurrencyAmountDTO outstandingAmount = currencyAmountMapper.mapToDto(
            new BigDecimal(invoiceCreationResultMessage.getOutstandingAmount()),
            invoiceCreationResultMessage.getOutstandingCurrency()
    );

    InvoiceContextDTO context = InvoiceContextDTO.builder()
            .customer(invoiceCreationResultMessage.getBuyerIdentifier())
            .theirReference(invoiceCreationResultMessage.getInvoiceNumber())
            .behalfOfBranch(invoiceCreationResultMessage.getBehalfOfBranch())
            .build();

    return InvoiceFinancingDTO.builder()
            .context(context)
            .anchorParty(invoiceCreationResultMessage.getAnchorPartyCustomerMnemonic())
            .programme(invoiceCreationResultMessage.getProgramme())
            .seller(invoiceCreationResultMessage.getSellerIdentifier())
            .sellerAccount(findSellerAccount(invoiceCreationResultMessage.getSellerIdentifier()).trim())
            .buyer(invoiceCreationResultMessage.getBuyerIdentifier())
            .maturityDate(maturityDateFormatted) // Mapeado desde el mensaje con formato
            .financeCurrency(invoiceCreationResultMessage.getFaceValueCurrency())
            .financePercent(new BigDecimal("100.00"))
            .financeDate(financeDateFormatted) // Mapeado desde el mensaje con formato
            .invoice(InvoiceNumberDTO.builder()
                    .number(invoiceCreationResultMessage.getInvoiceNumber())
                    .issueDate(financeDateFormatted)
                    .outstanding(outstandingAmount)
                    .build())
            .build();
  }


  private String findSellerAccount(String sellerIdentifier) {
    Account account = accountRepository.findByTypeAndCustomerMnemonic("CA", sellerIdentifier) // Asumimos tipo "CA" para cuentas
            .orElseThrow(
                    () ->
                            new EntityNotFoundException(
                                    "No se encontró la cuenta para el vendedor: " + sellerIdentifier));
    return account.getExternalAccountNumber(); // Devuelve el número de cuenta externa

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