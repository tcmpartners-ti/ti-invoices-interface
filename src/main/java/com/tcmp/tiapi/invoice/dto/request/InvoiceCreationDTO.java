package com.tcmp.tiapi.invoice.dto.request;

import com.tcmp.tiapi.shared.annotation.ValidDateFormat;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceCreationDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";
  private static final String DATE_REGEX = "^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-\\d{4}$";

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "The invoice context.")
  private InvoiceContextDTO context;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "The anchor party's mnemonic should be between 1 and 20 characters long")
  @Schema(description = "The anchor party's mnemonic.", minLength = 1, maxLength = 20)
  private String anchorParty;

  @NotNull(message = "This field is required.")
  @Size(max = 35, message = "The program id should be up to 35 characters long")
  @Schema(description = "Indicates the credit line to which the invoice relates.", maxLength = 35)
  private String programme;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "Credited party id must be between 1 and 20 characters long")
  @Schema(description = "The customer who is the credit party on the invoice(s).", minLength = 1, maxLength = 20)
  private String seller;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "Debited party id must be between 1 and 20 characters long")
  @Schema(description = "The customer who is the debit party on the invoice(s).", minLength = 1, maxLength = 20)
  private String buyer;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 34, message = "Invoice number must be between 1 and 34 characters long")
  @Schema(description = "The invoice number.", minLength = 1, maxLength = 34)
  private String invoiceNumber;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = DATE_REGEX, message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "The date the invoice(s) were issued.", format = DATE_FORMAT, type = "date")
  private String issueDate;

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "The total amount of the invoice in the denominated currency.")
  private CurrencyAmountDTO faceValue;

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "Face value amount less any related adjustments.")
  private CurrencyAmountDTO outstandingAmount;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = DATE_REGEX, message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "Invoice payment due date.", format = DATE_FORMAT, type = "string")
  private String settlementDate;
}
