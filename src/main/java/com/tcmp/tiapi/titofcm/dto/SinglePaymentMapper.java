package com.tcmp.tiapi.titofcm.dto;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.shared.utils.MapperUtils;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titofcm.dto.request.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    builder = @Builder(disableBuilder = true),
    imports = {List.class, OffsetDateTime.class},
    uses = {MapperUtils.class})
public abstract class SinglePaymentMapper {
  private static final int MAX_DESCRIPTION_LENGTH = 40;
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

  @Value("${bp.service.payment-execution.bgl-account}")
  protected String bglAccount;

  @Value("${fcm.config.debtor-id}")
  protected String debtorId;

  @Autowired protected Clock clock;

  @Mapping(target = "debtorIdentification", expression = "java(this.debtorId)")
  @Mapping(target = "legalEntity", constant = "ECU")
  @Mapping(target = "paymentReference", source = "message.masterRef")
  @Mapping(target = "transactionType", constant = TransactionType.CREDIT)
  @Mapping(target = "paymentbankproduct", constant = "Cadenas")
  @Mapping(target = "methodOfPayment", constant = "TI")
  @Mapping(
      target = "requestedExecutionDate",
      expression = "java(OffsetDateTime.now(clock))",
      dateFormat = DATE_FORMAT)
  @Mapping(
      target = "requestedExecutionTime",
      expression = "java(OffsetDateTime.now(clock))",
      dateFormat = DATE_FORMAT)
  @Mapping(target = "confidentialPayment", constant = "false")
  // Debtor is always BGL
  @Mapping(target = "debtorAccount.id.other.id", expression = "java(this.bglAccount)")
  @Mapping(target = "debtorAccount.type", constant = AccountType.GENERALLEDGER)
  @Mapping(target = "debtorAccount.currency", source = "message.paymentCurrency")
  @Mapping(target = "debtorAccount.name", expression = "java(this.bglAccount)")
  @Mapping(
      target = "instructedAmount.amount",
      expression = "java(getPaymentAmount(invoiceSettlementEventMessage))")
  @Mapping(target = "instructedAmount.currencyOfTransfer", source = "message.paymentCurrency")
  @Mapping(target = "chargeBearer", constant = "OUR")
  // Creditor
  @Mapping(target = "creditorDetails.creditorName", source = "creditor.fullName")
  @Mapping(target = "creditorDetails.account.id.other.id", source = "creditorAccount.account")
  @Mapping(target = "creditorDetails.account.type", constant = AccountType.SAVINGS)
  @Mapping(target = "creditorDetails.account.currency", source = "message.paymentCurrency")
  // Creditor Agent
  @Mapping(target = "creditorAgent.identifierType", constant = "NCC")
  @Mapping(target = "creditorAgent.name", constant = "Banco Pichincha Ecuador")
  @Mapping(target = "creditorAgent.otherId", constant = "0010")
  @Mapping(target = "creditorAgent.postalAddress.addressLine", expression = "java(List.of())")
  @Mapping(target = "creditorAgent.postalAddress.addressType", constant = "ADDR")
  @Mapping(target = "creditorAgent.postalAddress.country", constant = "EC")
  // Remittance Information
  @Mapping(
      target = "remittanceInformation",
      expression = "java(mapToRemittanceInformation(creditor, creditorAccount))")
  // Enrichment Details
  @Mapping(
      target = "enrichmentDetailsTransaction",
      expression =
          "java(mapToMultiSet(debitDescription, creditDescription, debtorAccount.getAccount()))")
  public abstract SinglePaymentRequest mapSettlementCustomerToCustomerTransaction(
      InvoiceSettlementEventMessage message,
      Customer debtor,
      Customer creditor,
      EncodedAccountParser debtorAccount,
      EncodedAccountParser creditorAccount,
      String debitDescription,
      String creditDescription);

  @Mapping(target = "debtorIdentification", expression = "java(this.debtorId)")
  @Mapping(target = "legalEntity", constant = "ECU")
  @Mapping(
      target = "paymentReference",
      expression = "java(message.getInvoiceArray().get(0).getInvoiceReference())")
  @Mapping(target = "transactionType", constant = TransactionType.CREDIT)
  @Mapping(target = "paymentbankproduct", constant = "Cadenas")
  @Mapping(target = "methodOfPayment", constant = "TI")
  @Mapping(
      target = "requestedExecutionDate",
      expression = "java(OffsetDateTime.now(clock))",
      dateFormat = DATE_FORMAT)
  @Mapping(
      target = "requestedExecutionTime",
      expression = "java(OffsetDateTime.now(clock))",
      dateFormat = DATE_FORMAT)
  @Mapping(target = "confidentialPayment", constant = "false")
  // Debtor is always BGL
  @Mapping(target = "debtorAccount.id.other.id", expression = "java(this.bglAccount)")
  @Mapping(target = "debtorAccount.type", constant = AccountType.GENERALLEDGER)
  @Mapping(target = "debtorAccount.currency", source = "message.outstandingCurrency")
  @Mapping(target = "debtorAccount.name", expression = "java(this.bglAccount)")
  @Mapping(target = "instructedAmount.amount", source = "amount")
  @Mapping(target = "instructedAmount.currencyOfTransfer", source = "message.outstandingCurrency")
  @Mapping(target = "chargeBearer", constant = "OUR")
  // Creditor
  @Mapping(target = "creditorDetails.creditorName", source = "creditor.fullName")
  @Mapping(target = "creditorDetails.account.id.other.id", source = "creditorAccount.account")
  @Mapping(target = "creditorDetails.account.type", constant = AccountType.SAVINGS)
  @Mapping(target = "creditorDetails.account.currency", source = "message.outstandingCurrency")
  // Creditor Agent
  @Mapping(target = "creditorAgent.identifierType", constant = "NCC")
  @Mapping(target = "creditorAgent.name", constant = "Banco Pichincha Ecuador")
  @Mapping(target = "creditorAgent.otherId", constant = "0010")
  @Mapping(target = "creditorAgent.postalAddress.addressLine", expression = "java(List.of())")
  @Mapping(target = "creditorAgent.postalAddress.addressType", constant = "ADDR")
  @Mapping(target = "creditorAgent.postalAddress.country", constant = "EC")
  // Remittance Information
  @Mapping(
      target = "remittanceInformation",
      expression = "java(mapToRemittanceInformation(creditor, creditorAccount))")
  // Enrichment Details
  @Mapping(
      target = "enrichmentDetailsTransaction",
      expression =
          "java(mapToMultiSet(debitDescription, creditDescription, debtorAccount.getAccount()))")
  public abstract SinglePaymentRequest mapFinanceCustomerToCustomerTransaction(
      FinanceAckMessage message,
      Customer debtor,
      Customer creditor,
      EncodedAccountParser debtorAccount,
      EncodedAccountParser creditorAccount,
      String debitDescription,
      String creditDescription,
      BigDecimal amount);

  public BigDecimal getPaymentAmount(InvoiceSettlementEventMessage invoiceSettlementMessage) {
    BigDecimal paymentAmountInCents = new BigDecimal(invoiceSettlementMessage.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);
  }

  @AfterMapping
  public void afterSettlementTransactionMapped(
      @MappingTarget SinglePaymentRequest singlePaymentRequest) {
    String debitDescription = singlePaymentRequest.getRemittanceInformation().getInformation2();
    String creditDescription = singlePaymentRequest.getRemittanceInformation().getInformation3();

    singlePaymentRequest
        .getRemittanceInformation()
        .setInformation2(sanitizeAndTrimDescription(debitDescription));

    singlePaymentRequest
        .getRemittanceInformation()
        .setInformation3(sanitizeAndTrimDescription(creditDescription));
  }

  private String sanitizeAndTrimDescription(String description) {
    String sanitizedDescription = description.replaceAll("[^a-zA-Z0-9\\s-]", "");

    if (sanitizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
      sanitizedDescription = sanitizedDescription.substring(0, MAX_DESCRIPTION_LENGTH);
    }

    return sanitizedDescription;
  }

  public EnrichmentDetailsTransaction mapToMultiSet(
      String debitDescription, String creditDescription, String debtorAccount) {
    EnrichmentDetailsTransaction.MultiSet multiSetItem =
        EnrichmentDetailsTransaction.MultiSet.builder()
            .debitDescription(debitDescription)
            .creditDescription(creditDescription)
            .debtorAccount(debtorAccount)
            .build();

    // Crear el objeto EnrichmentDetailsTransaction y establecer la lista multiSet
    EnrichmentDetailsTransaction transaction =
        EnrichmentDetailsTransaction.builder()
            .multiSet(
                Collections.singletonList(
                    multiSetItem)) // Devolvemos una lista con un solo elemento
            .build();

    return transaction;
  }

  public String mapInformation2(Customer creditor) {
    if (creditor.getBankCode1().equals("0001")) {
      return "C";
    } else {
      return "R";
    }
  }

  public RemittanceInformation mapToRemittanceInformation(
      Customer creditor, EncodedAccountParser creditorAccount) {
    String information2 = mapInformation2(creditor);
    String information3 = creditor.getOldMnemonic();
    String information4 = creditorAccount.getType().equals("AH") ? "AHO" : "CTE";

    return RemittanceInformation.builder()
        .information2(information2)
        .information3(information3)
        .information4(information4)
        .build();
  }
}
