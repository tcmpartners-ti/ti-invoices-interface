package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record DistributorCreditRequest(
  CommercialTrade commercialTrade,
  Customer customer,
  Disbursement disbursement,
  BigDecimal amount,
  String effectiveDate,
  String firstDueDate,
  Integer term,
  TermPeriodType termPeriodType,
  AmortizationPaymentPeriodType amortizationPaymentPeriodType,
  InterestPayment interestPayment,
  String maturityForm,
  String quotaMaturityCriteria,
  List<Reference> references,
  Tax tax
) {
}
