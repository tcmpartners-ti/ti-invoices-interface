package com.tcmp.tiapi.customer.controller;

import com.tcmp.tiapi.customer.dto.request.SearchBuyerInvoicesParams;
import com.tcmp.tiapi.customer.service.BuyerService;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("buyers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Buyers", description = "Defines the buyers (anchors) operations.")
public class BuyerController {
  private final BuyerService buyerService;


  @GetMapping(path = "{buyerMnemonic}/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get buyer's invoices by its mnemonic (ruc).")
  @Parameter(
          name = "status",
          description = "Invoice status to filter by. If not set, invoices with every status will be returned. Possible values: O,L,P,D,E,C.",
          schema = @Schema(type = "string"),
          in = ParameterIn.QUERY,
          example = "O")
  @Parameter(
          name = "page",
          description = "Page (0 based)",
          schema = @Schema(type = "number", defaultValue = "0"),
          in = ParameterIn.QUERY,
          example = "0")
  @Parameter(
          name = "size",
          description = "Page size (items per page).",
          schema = @Schema(type = "number", defaultValue = "10"),
          in = ParameterIn.QUERY,
          example = "10")
  public PaginatedResult<InvoiceDTO> getBuyerInvoicesByMnemonic(
          @PathVariable
          @Valid
          @NotNull(message = "This field is required.")
          @Size(min = 10, max = 13, message = "This field must have between 10 and 13 characters")
          @Pattern(
                  regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
                  message = "Only numeric values are allowed")
          @Pattern(
                  regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
                  message = "Special character are not allowed")
          String buyerMnemonic,
          @Parameter(hidden = true) @Valid SearchBuyerInvoicesParams searchParams,
          @Parameter(hidden = true) @Valid PageParams pageParams) {
      return buyerService.getBuyerInvoices(buyerMnemonic, searchParams, pageParams);
  }

  @GetMapping(path = "{buyerMnemonic}/programs", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get the buyer's (anchor's) programs by its mnemonic.")
  @Parameter(
      name = "page",
      description = "Page (0 based).",
      schema = @Schema(type = "number"),
      in = ParameterIn.QUERY,
      example = "0")
  @Parameter(
      name = "size",
      description = "Page size (items per page).",
      schema = @Schema(type = "number"),
      in = ParameterIn.QUERY,
      example = "10")
  public PaginatedResult<ProgramDTO> getBuyerProgramsByMnemonic(
      @Parameter(hidden = true) @Valid PageParams pageParams,
      @PathVariable
          @Valid
          @NotNull(message = "This field is required.")
          @Size(min = 10, max = 13, message = "This field must have between 10 and 13 characters")
          @Pattern(
              regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
              message = "Only numeric values are allowed")
          @Pattern(
              regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
              message = "Special characters are not allowed")
          String buyerMnemonic) {
    return buyerService.getBuyerProgramsByMnemonic(buyerMnemonic, pageParams);
  }
}
