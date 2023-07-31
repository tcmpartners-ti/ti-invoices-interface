package com.tcmp.tiapi.program.dto.request;

import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramCreationDTO {
    @NotBlank(message = "Program description is required.")
    @Size(min = 1, max = 60, message = "Program description must be between 1 and 60 characters.")
    private String description;

    private ProgramCustomerDTO customer;

    @FutureOrPresent(message = "Date must be present or future")
    private LocalDate startDate;

    @Future(message = "Date must be in the future")
    private LocalDate endDate;

    @Pattern(regexp = "[BS]", message = "Type must be 'B' or 'S' (Buyer or Seller centric)")
    private String type;

    private CurrencyAmountDTO creditLimit;

    @Pattern(regexp = "[ABIR]", message = "Status must be either 'A', 'B', 'I' or 'R' ('Active', 'Blocked', 'Inactive' or 'Referred')")
    private String status;
}
