package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tcmp.tiapi.shared.serializer.JsonMoneySerializer;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DistributorCreditRequest(
  CommercialTrade commercialTrade,
  Customer customer,
  Disbursement disbursement,
  @JsonSerialize(using = JsonMoneySerializer.class)
  BigDecimal amount,
  String effectiveDate,
  @JsonInclude(JsonInclude.Include.NON_NULL)
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
