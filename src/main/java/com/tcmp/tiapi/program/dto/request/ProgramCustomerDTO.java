package com.tcmp.tiapi.program.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramCustomerDTO {
    @NotBlank(message = "Customer mnemonic is required.")
    @Size(min = 1, max = 20, message = "Customer mnemonic must be between 1 and 20 characters.")
    private String mnemonic;
}
