package com.tcmp.tiapi.program.dto.response;

import com.tcmp.tiapi.program.dto.request.ProgramCustomerDTO;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramDTO {
    private String id;

    private String name;

    private ProgramCustomerDTO customer;

    private LocalDate startDate;

    private LocalDate endDate;

    private String type;

    private CurrencyAmountDTO creditLimit;

    private String status;
}
