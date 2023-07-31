package com.tcmp.tiapi.invoice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceContextDTO {
    @NotBlank(message = "Customer's mnemonic is required")
    @Size(min = 1, max = 20, message = "Customer's mnemonic must be between 1 and 20 characters long")
    @Schema(name = "customer", description = "Sender's customer mnemonic.", minLength = 1, maxLength = 20)
    private String customer;

    @Size(min = 1, max = 34, message = "Their reference must be between 1 and 34 characters long")
    @Schema(name = "theirReference", description = "Sender's reference for this transaction (if known).", minLength = 1, maxLength = 34)
    private String theirReference;
}
