package com.tcmp.tiapi.titoapigee.corporateloan.dto;

import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.shared.utils.MapperUtils;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    builder = @Builder(disableBuilder = true),
    imports = {List.class, BigDecimal.class, MonetaryAmountUtils.class},
    uses = {MapperUtils.class})
public abstract class CorporateLoanMapper {

  @Mapping(target = "commercialTrade.code", source = "buyer.type")
  // Customer
  @Mapping(target = "customer.customerId", source = "buyer.number")
  @Mapping(target = "customer.documentNumber", source = "buyer.id.mnemonic")
  @Mapping(target = "customer.fullName", source = "buyer.fullName")
  @Mapping(target = "customer.documentType", source = "buyer.bankCode1")
  // Disbursement
  @Mapping(target = "disbursement.accountNumber", source = "buyerAccount.account")
  @Mapping(target = "disbursement.accountType", source = "buyerAccount.type")
  @Mapping(target = "disbursement.bankId", constant = "010")
  @Mapping(target = "disbursement.form", constant = "N/C")
  //
  @Mapping(target = "amount", expression = "java(getFinanceDealAmount(message))")
  @Mapping(target = "firstDueDate", ignore = true)
  @Mapping(target = "effectiveDate", source = "message.startDate")
  @Mapping(target = "term", source = "term")
  @Mapping(target = "termPeriodType.code", constant = "D")
  @Mapping(target = "amortizationPaymentPeriodType.code", constant = "FIN")
  @Mapping(target = "interestPayment.code", constant = "FIN")
  @Mapping(target = "interestPayment.gracePeriod.code", constant = "V")
  @Mapping(target = "interestPayment.gracePeriod.installmentNumber", constant = "001")
  @Mapping(target = "maturityForm", constant = "C99")
  @Mapping(target = "quotaMaturityCriteria", constant = "*NO")
  @Mapping(target = "references", expression = "java(List.of())")
  @Mapping(target = "tax.code", constant = "L")
  @Mapping(target = "tax.paymentForm.code", constant = "C")
  @Mapping(target = "tax.rate", expression = "java(BigDecimal.ZERO)")
  @Mapping(target = "tax.amount", expression = "java(BigDecimal.ZERO)")
  public abstract DistributorCreditRequest mapToFinanceRequest(
      FinanceAckMessage message,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccount,
      int term);

  protected BigDecimal getFinanceDealAmount(FinanceAckMessage message) {
    BigDecimal financeDealAmountInCents = new BigDecimal(message.getFinanceDealAmount());
    return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
  }

  @Mapping(target = "commercialTrade.code", source = "buyer.type")
  // Customer
  @Mapping(target = "customer.customerId", source = "buyer.number")
  @Mapping(target = "customer.documentNumber", source = "buyer.id.mnemonic")
  @Mapping(target = "customer.fullName", source = "buyer.fullName")
  @Mapping(target = "customer.documentType", source = "buyer.bankCode1")
  // Disbursement
  @Mapping(target = "disbursement.accountNumber", source = "buyerAccount.account")
  @Mapping(target = "disbursement.accountType", source = "buyerAccount.type")
  @Mapping(target = "disbursement.bankId", constant = "010")
  @Mapping(target = "disbursement.form", constant = "N/C")
  //
  @Mapping(target = "amount", expression = "java(getPaymentAmount(message))")
  @Mapping(target = "firstDueDate", ignore = true)
  @Mapping(target = "effectiveDate", source = "paymentValueDate")
  @Mapping(target = "term", source = "programExtension.extraFinancingDays")
  @Mapping(target = "termPeriodType.code", constant = "D")
  @Mapping(target = "amortizationPaymentPeriodType.code", constant = "FIN")
  @Mapping(target = "interestPayment.code", constant = "FIN")
  @Mapping(target = "interestPayment.gracePeriod.code", constant = "V")
  @Mapping(target = "interestPayment.gracePeriod.installmentNumber", constant = "001")
  @Mapping(target = "maturityForm", constant = "C99")
  @Mapping(target = "quotaMaturityCriteria", constant = "*NO")
  @Mapping(target = "references", expression = "java(List.of())")
  @Mapping(target = "tax.code", constant = "L")
  @Mapping(target = "tax.paymentForm.code", constant = "C")
  @Mapping(target = "tax.rate", expression = "java(BigDecimal.ZERO)")
  @Mapping(target = "tax.amount", expression = "java(BigDecimal.ZERO)")
  public abstract DistributorCreditRequest mapToFinanceRequest(
      InvoiceSettlementEventMessage message,
      Customer buyer,
      EncodedAccountParser buyerAccount,
      String paymentValueDate,
      ProgramExtension programExtension);

  protected BigDecimal getPaymentAmount(InvoiceSettlementEventMessage message) {
    BigDecimal paymentAmountInCents = new BigDecimal(message.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);
  }
}
