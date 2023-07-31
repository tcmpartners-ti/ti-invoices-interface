package com.tcmp.tiapi.invoice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceCreatedDTO {
    @Schema(name = "message", description = "Operation result message.")
    private String message;

    @Schema(name = "invoice", description = "Basic invoice information.")
    private InvoiceDTO invoice;

    @Data
    @AllArgsConstructor
    public static class InvoiceDTO {
        @Schema(name = "id", description = "Generated UUID. (TBD)")
        private String id;
    }
}
