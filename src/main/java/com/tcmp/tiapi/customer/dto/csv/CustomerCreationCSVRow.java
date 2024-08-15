package com.tcmp.tiapi.customer.dto.csv;

import com.opencsv.bean.CsvBindByName;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;

@CsvRecord(separator = ";")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCreationCSVRow implements Serializable {
  private static final String NUMBER_FORMAT = "#.##";
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @CsvBindByName(column = "SourceBankingBusiness", required = true)
  @Size(min = 1, max = 8, message = "SourceBankingBusiness must be between 1 and 8 characters")
  private String sourceBankingBusiness;

  @CsvBindByName(column = "Branch", required = true)
  @Size(min = 1, max = 8, message = "Branch must be between 1 and 8 characters")
  private String branch;

  @CsvBindByName(column = "Mnemonic", required = true)
  @Size(min = 10, max = 13, message = "Mnemonic must be between 10 and 13 characters")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Mnemonic must be numeric")
  private String mnemonic;

  @CsvBindByName(column = "BankCode", required = true)
  @Pattern(regexp = "0001|0003", message = "BankCode must be 0001 or 0003")
  private String bankCode;

  @CsvBindByName(column = "Number", required = true)
  @Size(min = 2, max = 12, message = "Number must be between 2 and 12 characters")
  private String number;

  @CsvBindByName(column = "Type", required = true)
  @Size(min = 2, max = 8, message = "Type must be between 2 and 8 characters")
  private String type; // Commercial Destination in GAF

  @CsvBindByName(column = "FullName", required = true)
  @Size(min = 1, max = 35, message = "FullName must be between 1 and 35 characters")
  private String fullName;

  @CsvBindByName(column = "ShortName", required = true)
  @Size(min = 1, max = 15, message = "ShortName must be between 1 and 15 characters")
  private String shortName;

  @CsvBindByName(column = "Address", required = true)
  @Size(max = 1024, message = "Address must be less than 1024 characters")
  private String address;

  @CsvBindByName(column = "Phone", required = true)
  @Size(min = 2, max = 20, message = "Phone must be between 2 and 20 characters")
  private String phone;

  @CsvBindByName(column = "Email", required = true)
  @Size(min = 2, max = 128, message = "Email must be between 2 and 128 characters")
  private String email;

  @CsvBindByName(column = "AccountType", required = true)
  @Size(min = 1, max = 10, message = "AccountType must be between 1 and 10 characters")
  @Pattern(regexp = "^CA$", message = "AccountType must be CA")
  private String accountType;

  @CsvBindByName(column = "AccountNumber", required = true)
  @Size(min = 1, max = 34, message = "AccountNumber must be between 1 and 34 characters")
  @Pattern(
      regexp = FieldValidationRegex.BP_BANK_ACCOUNT,
      message = "AccountNumber must begin with AH or CC followed by 10 digits")
  private String accountNumber;

  @CsvBindByName(column = "AccountCurrency", required = true)
  @Size(min = 3, max = 3, message = "AccountCurrency must be 3 characters long")
  @Pattern(regexp = "^USD$", message = "AccountCurrency must be USD")
  private String accountCurrency;

  @CsvBindByName(column = "AccountDateOpened", required = true)
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Date must be in the format " + DATE_FORMAT)
  private String accountDateOpened;
}
