package com.tcmp.tiapi.invoice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceFinancingDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Valid
  @Schema
  private InvoiceContextDTO context;

  @Size(max = 35, message = "The program id should be up to 35 characters long")
  @Schema(maxLength = 35, description = "Indicates the credit line to which the invoice relates.")
  private String programme;

  @Size(min = 1, max = 20, message = "Credited party id must be between 1 and 20 characters long")
  @Schema(minLength = 1, maxLength = 20, description = "The customer who is the credit party on the invoice(s).")
  private String seller;

  @Size(min = 1, max = 20, message = "Debited party id must be between 1 and 20 characters long")
  @Schema(minLength = 1, maxLength = 20, description = "The customer who is the debit party on the invoice(s).")
  private String buyer;

  @Size(min = 1, max = 20, message = "The anchor party's mnemonic should be between 1 and 20 characters long")
  @Schema(description = "The anchor party's mnemonic.", minLength = 1, maxLength = 20)
  private String anchorParty;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "The date the invoice(s) will become mature.", format = DATE_FORMAT)
  private LocalDate maturityDate;

  @Size(min = 1, max = 3, message = "Finance currency code must be between 1 and 3 characters.")
  @Schema(minLength = 1, maxLength = 3, description = "The currency of the advance - the invoice(s) currency is used as the default.")
  private String financeCurrency;

  @Schema(description = "The percentage of the amount to be financed. Available Values: 0 to 100.")
  @Digits(integer = 15, fraction = 2, message = "Up to 15 integer digits and 2 fractional digits are allowed")
  private String financePercent;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "The start date of the advance - required for interest calculation.", format = DATE_FORMAT)
  private LocalDate financeDate;

  @Valid
  InvoiceNumberDTO invoice;
}
