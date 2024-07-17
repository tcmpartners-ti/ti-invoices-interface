package com.tcmp.tiapi.program.dto.csv;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseRateCsvRow {

    private static final String NUMBER_FORMAT = "#.##";

    @CsvBindByName(column = "ProgrammeID")
    @Size(min = 1, max = 35, message = "Id must be between 1 and 35 characters")
    private String programmeId;

    @CsvBindByName(column = "Seller")
    @Size(min = 1, max = 20, message = "Seller mnemonic must be between 1 and 20 character")
    private String seller;

    @CsvBindByName(column = "Buyer")
    @Size(min = 1, max = 20, message = "Buyer mnemonic must be between 1 and 20 character")
    private String buyer;

    @CsvBindByName(column = "BaseRate")
    @Size(min = 2, max = 5, message = "Base Rate must be between 2 and 5 characters")
    private String rate;


}
