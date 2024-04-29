package com.tcmp.tiapi.titofcm.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SinglePaymentRequest {
  private String debtorIdentification;
  private String legalEntity;
  private String paymentReference;
  private String transactionType;
  private String paymentbankproduct;
  private String methodOfPayment;
  private OffsetDateTime requestedExecutionDate;
  private OffsetDateTime requestedExecutionTime;

  @JsonProperty("isConfidentialPayment")
  private boolean isConfidentialPayment;

  private String chargeBearer;
  private Account debtorAccount;
  private InstructedAmountCurrencyOfTransfer2 instructedAmount;
  private CreditorDetails creditorDetails;
  private CreditorAgent creditorAgent;
  private RemittanceInformation remittanceInformation;
}
