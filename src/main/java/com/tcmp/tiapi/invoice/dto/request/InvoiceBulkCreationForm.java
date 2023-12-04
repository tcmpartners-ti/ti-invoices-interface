package com.tcmp.tiapi.invoice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record InvoiceBulkCreationForm(
    @RequestPart
        @Schema(description = "Invoices file in CSV format.")
        @NotNull(message = "Invoices file is required.")
        MultipartFile invoicesFile,
    @RequestPart
        @Schema(maxLength = 20, description = "Invoices batch identifier. (max 20 characters)")
        @NotNull(message = "This field is required.")
        @Size(min = 1, max = 20, message = "This field must be up to 20 characters.")
        @Pattern(
            regexp = "^[a-zA-Z0-9_]*$",
            message = "Only letters, numbers and underscores are allowed.")
        String batchId) {}
