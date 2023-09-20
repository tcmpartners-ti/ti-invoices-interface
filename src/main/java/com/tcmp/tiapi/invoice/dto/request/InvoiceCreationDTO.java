package com.tcmp.tiapi.invoice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
public class InvoiceCreationDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Valid
  @Schema
  private InvoiceContextDTO context;

  @Size(min = 1, max = 20, message = "The anchor party's mnemonic should be between 1 and 20 characters long")
  @Schema(description = "The anchor party's mnemonic.", minLength = 1, maxLength = 20)
  private String anchorParty;

  @Size(max = 35, message = "The program id should be up to 35 characters long")
  @Schema(description = "Indicates the credit line to which the invoice relates.", maxLength = 35)
  private String programme;

  @Size(min = 1, max = 20, message = "Credited party id must be between 1 and 20 characters long")
  @Schema(description = "The customer who is the credit party on the invoice(s).", minLength = 1, maxLength = 20)
  private String seller;

  @Size(min = 1, max = 20, message = "Debited party id must be between 1 and 20 characters long")
  @Schema(description = "The customer who is the debit party on the invoice(s).", minLength = 1, maxLength = 20)
  private String buyer;

  @Size(min = 1, max = 34, message = "Invoice number must be between 1 and 34 characters long")
  private String invoiceNumber;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "The date the invoice(s) were issued.", format = DATE_FORMAT)
  private LocalDate issueDate;

  @Valid
  @Schema(description = "The total amount of the invoice in the denominated currency.")
  private CurrencyAmountDTO faceValue;

  @Valid
  @Schema(description = "Face value amount less any related adjustments.")
  private CurrencyAmountDTO outstandingAmount;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "Invoice payment due date.", format = DATE_FORMAT)
  private LocalDate settlementDate;
}
