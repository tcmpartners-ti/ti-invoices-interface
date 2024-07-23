package com.tcmp.tiapi.invoice.dto.request;

import com.tcmp.tiapi.shared.FieldValidationRegex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GenerateInvoiceReportParams(
    @NotNull
        @Pattern(
            regexp = FieldValidationRegex.INVOICE_NUMBER,
            message = "Field must contain numbers only.")
        @Size(min = 10, max = 13, message = "Field must be between 10 to 13 characters.")
        String customerMnemonic,
    @NotNull @Pattern(regexp = "^(BUYER|SELLER)$") String customerRole) {}
