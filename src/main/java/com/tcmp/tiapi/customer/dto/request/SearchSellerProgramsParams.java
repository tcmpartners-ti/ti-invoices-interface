package com.tcmp.tiapi.customer.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;

public record SearchSellerProgramsParams(
    @Nullable
        @Pattern(
            regexp = "^(CIF|MNEMONIC)$",
            message = "Possible values for this field are 'CIF' or 'MNEMONIC'.")
        String type) {}
