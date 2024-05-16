package com.tcmp.tiapi.program.dto.csv;

import com.opencsv.bean.CsvBindByName;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;

@CsvRecord(separator = ",")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramCreationCsvRow {
  private static final String NUMBER_FORMAT = "#.##";
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @CsvBindByName(column = "SourceBankingBusiness", required = true)
  @Size(min = 1, max = 8, message = "SourceBankingBusiness must be between 1 and 8 characters")
  private String sourceBankingBusiness;

  @CsvBindByName(column = "Branch", required = true)
  @Size(min = 1, max = 8, message = "Branch must be between 1 and 8 characters")
  private String branch;

  @CsvBindByName(column = "ProgrammeId")
  @Size(min = 1, max = 35, message = "Id must be between 1 and 35 characters")
  private String programmeId;

  @CsvBindByName(column = "ProgrammeDescription")
  @Size(min = 1, max = 60, message = "Description must be between 1 and 60 characters")
  private String programmeDescription;

  @CsvBindByName(column = "ProgrammeType")
  @Size(min = 1, max = 1, message = "Programme type must be 1 character long")
  @Pattern(regexp = "[BS]", message = "Programme type must be either B or S")
  private String programmeType;

  @CsvBindByName(column = "ProgrammeSubtype")
  @Size(min = 1, max = 10, message = "Programme subtype must be between 1 and 10 characters")
  private String programmeSubtype;

  @CsvBindByName(column = "CreditLimitCurrency")
  @Pattern(
      regexp = "^[A-Z]{1,3}$",
      message = "Credit limit currency must be 1 to 3 uppercase letters")
  private String creditLimitCurrency;

  @CsvBindByName(column = "CreditLimitAmount", format = NUMBER_FORMAT)
  @Pattern(
      regexp = FieldValidationRegex.NUMBER_WITH_DECIMALS,
      message = "This field must be in format " + NUMBER_FORMAT)
  private String creditLimitAmount;

  @CsvBindByName(column = "ProgrammeStartDate")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Programme start date must be in the format " + DATE_FORMAT)
  private String programmeStartDate;

  @CsvBindByName(column = "ProgrammeExpiryDate")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Programme expiry date must be in the format " + DATE_FORMAT)
  private String programmeExpiryDate;

  @CsvBindByName(column = "FinanceProduct")
  private String financeProduct;

  @CsvBindByName(column = "FinanceProductType")
  private String financeProductType;

  @CsvBindByName(column = "FinanceParameterSet")
  private String financeParameterSet;

  @CsvBindByName(column = "BuyerMnemonic")
  @Size(min = 1, max = 20, message = "Buyer mnemonic must be between 1 and 20 characters")
  private String buyerMnemonic;

  @CsvBindByName(column = "BuyerName")
  @Size(min = 1, max = 35, message = "Buyer name must be between 1 and 35 characters")
  private String buyerName;

  @CsvBindByName(column = "BuyerInvoiceLimitCurrency")
  @Pattern(
      regexp = "^[A-Z]{1,3}$",
      message = "Buyer invoice limit currency must be 1 to 3 uppercase letters")
  private String buyerInvoiceLimitCurrency;

  @CsvBindByName(column = "BuyerInvoiceLimitAmount")
  @Pattern(
      regexp = FieldValidationRegex.NUMBER_WITH_DECIMALS,
      message = "This field must be in format " + NUMBER_FORMAT)
  private String buyerInvoiceLimitAmount;

  @CsvBindByName(column = "SellerMnemonic")
  @Size(min = 1, max = 20, message = "Seller mnemonic must be between 1 and 20 characters")
  private String sellerMnemonic;

  @CsvBindByName(column = "SellerName")
  @Size(min = 1, max = 35, message = "Seller name must be between 1 and 35 characters")
  private String sellerName;

  @CsvBindByName(column = "SellerInvoiceLimitCurrency")
  @Pattern(
      regexp = "^[A-Z]{1,3}$",
      message = "Seller invoice limit currency must be 1 to 3 uppercase letters")
  private String sellerInvoiceLimitCurrency;

  @CsvBindByName(column = "SellerInvoiceLimitAmount")
  @Pattern(
      regexp = FieldValidationRegex.NUMBER_WITH_DECIMALS,
      message = "Seller invoice limit amount must be in format " + NUMBER_FORMAT)
  private String sellerInvoiceLimitAmount;

  @CsvBindByName(column = "CustomerBuyerLimitCurrency")
  @Pattern(
      regexp = "^[A-Z]{1,3}$",
      message = "Customer buyer limit currency must be 1 to 3 uppercase letters")
  private String customerBuyerLimitCurrency;

  @CsvBindByName(column = "CustomerBuyerLimitAmount")
  @Pattern(
      regexp = FieldValidationRegex.NUMBER_WITH_DECIMALS,
      message = "Customer buyer limit amount must be in format " + NUMBER_FORMAT)
  private String customerBuyerLimitAmount;

  @CsvBindByName(column = "BuyerPercent", format = NUMBER_FORMAT)
  @DecimalMax(value = "100", message = "Buyer percent must be less than or equal to 100")
  @DecimalMin(value = "0", message = "Buyer percent must be greater than or equal to 0")
  private BigDecimal buyerPercent;

  @CsvBindByName(column = "MaximumPeriodDays", format = "#")
  @Min(value = 0, message = "Maximum period days must be greater than or equal to 0")
  private Integer maximumPeriodDays;

  @CsvBindByName(column = "MaximumPeriod")
  @Pattern(regexp = "[DMQWY]", message = "Maximum period must be D, M, Q, W, or Y")
  private String maximumPeriod;
}
