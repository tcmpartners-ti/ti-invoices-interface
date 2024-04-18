package com.tcmp.tiapi.titofcm.dto;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.shared.utils.MapperUtils;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titofcm.dto.request.AccountType;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.request.TransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Value;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    imports = {List.class, OffsetDateTime.class},
    uses = {MapperUtils.class})
public abstract class SinglePaymentMapper {
  private static final int MAX_DESCRIPTION_LENGTH = 40;
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

  @Value("${bp.service.payment-execution.bgl-account}")
  protected String bglAccount;

  @Value("${fcm.config.debtor-id}")
  protected String debtorId;

  @Value("${fcm.config.short-name}")
  protected String shortName;

  @Mapping(target = "debtorIdentification", expression = "java(this.debtorId)")
  @Mapping(target = "legalEntity", constant = "ECU")
  @Mapping(target = "paymentReference", source = "message.masterRef")
  @Mapping(target = "transactionType", constant = TransactionType.CREDIT)
  @Mapping(target = "paymentbankproduct", constant = "Cadenas")
  @Mapping(target = "methodOfPayment", constant = "TI")
  @Mapping(
      target = "requestedExecutionDate",
      expression = "java(OffsetDateTime.now())",
      dateFormat = DATE_FORMAT)
  @Mapping(
      target = "requestedExecutionTime",
      expression = "java(OffsetDateTime.now())",
      dateFormat = DATE_FORMAT)
  @Mapping(target = "confidentialPayment", constant = "false")
  // Debtor is always BGL
  @Mapping(target = "debtorAccount.id.other.id", expression = "java(this.bglAccount)")
  @Mapping(target = "debtorAccount.type", constant = AccountType.SAVINGS)
  @Mapping(target = "debtorAccount.currency", source = "message.paymentCurrency")
  @Mapping(target = "debtorAccount.name", expression = "java(this.shortName)")
  @Mapping(
      target = "instructedAmountCurrencyOfTransfer2.amount",
      expression = "java(getPaymentAmountFromMessage(invoiceSettlementEventMessage))")
  @Mapping(
      target = "instructedAmountCurrencyOfTransfer2.currencyOfTransfer",
      source = "message.paymentCurrency")
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
  @Mapping(target = "remittanceInformation.information2", source = "debitDescription")
  @Mapping(target = "remittanceInformation.information3", source = "creditDescription")
  @Mapping(target = "remittanceInformation.information4", source = "debtorAccount.account")
  public abstract SinglePaymentRequest mapCustomerToCustomerSettlementTransaction(
      InvoiceSettlementEventMessage message,
      Customer debtor,
      Customer creditor,
      EncodedAccountParser debtorAccount,
      EncodedAccountParser creditorAccount,
      String debitDescription,
      String creditDescription);

  public BigDecimal getPaymentAmountFromMessage(
      InvoiceSettlementEventMessage invoiceSettlementMessage) {
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
}
