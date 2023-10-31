package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.request.TemplateField;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePrepaymentService {
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;

  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;

  @Value("${bp.service.payment-execution.bgl-account}") private String bglAccount;
  @Value("${bp.service.operational-gateway.template-id}") private String templateId;

  public void createInvoicePrepaymentCredit(FinanceAckMessage financeAckMessage) {
    com.tcmp.tiapi.customer.model.Customer buyer = findCustomerByMnemonic(financeAckMessage.getBuyerIdentifier());
    com.tcmp.tiapi.customer.model.Customer seller = findCustomerByMnemonic(financeAckMessage.getSellerIdentifier());

    notifyInvoiceEventToSeller(financeAckMessage, seller.getAddress().getCustomerEmail(), "Anticipo");

    String encodedBuyerAccount = findFinanceAccountByMasterRef(financeAckMessage.getMasterRef());
    EncodedAccountParser buyerAccount = new EncodedAccountParser(encodedBuyerAccount);

    DistributorCreditRequest distributorCreditRequest = buildRequestFromTiMessage(
      financeAckMessage, buyer, buyerAccount.getAccount(), buyerAccount.getType());
    DistributorCreditResponse corporateLoanResponse = corporateLoanService.createCredit(distributorCreditRequest);

    Account sellerAccount = findCustomerAccount(financeAckMessage.getSellerIdentifier());

    boolean buyerToSellerTransactionExecutedSuccessfully = transferAmountFromBuyerToSeller(
      buyerAccount.getAccount(),
      sellerAccount.getExternalAccountNumber(),
      financeAckMessage.getSellerName(),
      financeAckMessage.getTheirRef(),
      corporateLoanResponse.data().disbursementAmount()
    );

    if (buyerToSellerTransactionExecutedSuccessfully) {
      notifyInvoiceEventToSeller(financeAckMessage, seller.getAddress().getCustomerEmail(), "Procesamiento");
      log.info("Invoice prepayment flow finished successfully.");
    }
  }

  private com.tcmp.tiapi.customer.model.Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository.findFirstByIdMnemonic(customerMnemonic)
      .orElseThrow(() -> new RuntimeException("Could not find customer with mnemonic " + customerMnemonic));
  }

  private String findFinanceAccountByMasterRef(String invoiceMasterReference) {
    return productMasterExtensionRepository.findFinanceAccountByMasterReference(invoiceMasterReference)
      .orElseThrow(() -> new RuntimeException("Could not find account for the given invoice master."));
  }

  private Account findCustomerAccount(String customerMnemonic) {
    return accountRepository.findByTypeAndCustomerMnemonic("CA", customerMnemonic)
      .orElseThrow(() -> new RuntimeException("Could not find account for seller " + customerMnemonic));
  }

  /**
   * GAF
   */
  private DistributorCreditRequest buildRequestFromTiMessage(
    FinanceAckMessage financeAckMessage,
    com.tcmp.tiapi.customer.model.Customer buyer,
    String buyerAccountNumber,
    String buyerAccountType
  ) {
    return DistributorCreditRequest.builder()
      .commercialTrade(new CommercialTrade(buyer.getType()))
      .customer(Customer.builder()
        .customerId(buyer.getNumber())
        .documentNumber(buyer.getId().getMnemonic())
        .fullName(buyer.getFullName())
        .documentType("0003")
        .build())
      .disbursement(Disbursement.builder()
        .accountNumber(buyerAccountNumber)
        .accountType(buyerAccountType)
        .bankId("010")
        .form("N/C")
        .build())
      .amount(getAmountFromFinanceAckMessage(financeAckMessage))
      .effectiveDate(financeAckMessage.getReceivedOn())
      .firstDueDate(financeAckMessage.getMaturityDate())
      .term(0)
      .termPeriodType(new TermPeriodType("D"))
      .amortizationPaymentPeriodType(new AmortizationPaymentPeriodType("FIN"))
      .interestPayment(new InterestPayment("FIN", new GracePeriod("O", "002")))
      .maturityForm("FIJ")
      .quotaMaturityCriteria("*NO")
      .references(List.of())
      .tax(Tax.builder()
        .code("L")
        .paymentForm(new PaymentForm("D"))
        .rate(BigDecimal.ZERO)
        .amount(BigDecimal.ZERO)
        .build())
      .build();
  }

  /**
   * Payment Execution
   */
  private boolean transferAmountFromBuyerToSeller(
    String anchorAccount,
    String sellerAccount,
    String sellerName,
    String invoiceReference,
    double disbursementAmount
  ) {
    String transactionConcept = buildTransactionConcept(invoiceReference, sellerName);
    BigDecimal amount = BigDecimal.valueOf(disbursementAmount);

    BusinessAccountTransfersResponse buyerToBglResponse;

    try {
      buyerToBglResponse = paymentExecutionService.executeTransaction(
        TransactionType.CLIENT_TO_BGL,
        anchorAccount,
        bglAccount,
        transactionConcept,
        amount
      );
    } catch (PaymentExecutionException e) {
      return false;
    }

    BusinessAccountTransfersResponse bglToSellerResponse;

    try {
      bglToSellerResponse = paymentExecutionService.executeTransaction(
        TransactionType.BGL_TO_CLIENT,
        bglAccount,
        sellerAccount,
        transactionConcept,
        amount
      );
    } catch (PaymentExecutionException e) {
      return false;
    }

    if (buyerToBglResponse.data() == null || bglToSellerResponse.data() == null) {
      return false;
    }

    String buyerToBglStatus = buyerToBglResponse.data().status();
    String bglToSellerStatus = bglToSellerResponse.data().status();

    log.info("Anchor -> Bgl [{}]; Bgl -> Seller [{}]", buyerToBglStatus, bglToSellerStatus);

    return "OK".equals(buyerToBglStatus) && "OK".equals(bglToSellerStatus);
  }

  private String buildTransactionConcept(String invoiceReference, String sellerName) {
    return String.format("Pago Factura %s %s", invoiceReference, sellerName);
  }

  /**
   * Operative Gateway
   */
  private void notifyInvoiceEventToSeller(
    FinanceAckMessage financeAckMessage,
    String sellerEmail,
    String invoiceAction
  ) {
    operationalGatewayService.sendEmailNotification(
      financeAckMessage.getSellerIdentifier(),
      sellerEmail,
      templateId,
      operationalGatewayService.buildInvoiceEventEmailTemplate(
        financeAckMessage.getSellerIdentifier(),
        financeAckMessage.getSellerName(),
        financeAckMessage.getReceivedOn(),
        invoiceAction,
        financeAckMessage.getTheirRef(),
        financeAckMessage.getFinanceDealCurrency(),
        getAmountFromFinanceAckMessage(financeAckMessage)
      )
    );
  }

  /**
   * SHARED
   */
  private BigDecimal getAmountFromFinanceAckMessage(FinanceAckMessage financeAckMessage) {
    BigDecimal financeDealAmountInCents = new BigDecimal(financeAckMessage.getFinanceDealAmount());
    return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
  }
}
