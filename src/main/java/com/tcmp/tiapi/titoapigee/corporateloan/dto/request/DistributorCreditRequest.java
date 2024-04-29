package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tcmp.tiapi.shared.serializer.JsonMoneySerializer;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistributorCreditRequest {
  private CommercialTrade commercialTrade;
  private Customer customer;
  private Disbursement disbursement;

  @JsonSerialize(using = JsonMoneySerializer.class)
  private BigDecimal amount;

  private String effectiveDate;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String firstDueDate;

  private Integer term;
  private TermPeriodType termPeriodType;
  private AmortizationPaymentPeriodType amortizationPaymentPeriodType;
  private InterestPayment interestPayment;
  private String maturityForm;
  private String quotaMaturityCriteria;
  private List<Reference> references;
  private Tax tax;
}
