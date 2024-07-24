package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.*;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCreatedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceFinancedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoicesCreatedDTO;
import com.tcmp.tiapi.invoice.exception.InvoiceReportException;
import com.tcmp.tiapi.invoice.service.InvoiceBatchOperationsService;
import com.tcmp.tiapi.invoice.service.InvoiceService;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("invoices")
@RequiredArgsConstructor
@Validated
@Tag(name = "Invoices", description = "Defines the invoices operations.")
@Slf4j
public class InvoiceController {
  private final InvoiceService invoiceService;
  private final InvoiceBatchOperationsService invoiceBatchOperationsService;

  @GetMapping(path = "{invoiceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get an invoice by its internal id.")
  public InvoiceDTO getInvoiceById(
      @PathVariable
          @Valid
          @Pattern(
              regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
              message = "Only numeric values are allowed")
          @Size(max = 20, message = "This field must have up to 20 characters")
          String invoiceId) {
    return invoiceService.getInvoiceById(Long.parseLong(invoiceId));
  }

  @GetMapping(path = "search", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Search an invoice by its programme, seller and number.")
  @Parameter(
      name = "programme",
      description = "Indicates the credit line to which the invoice relates.",
      schema = @Schema(type = "string"),
      in = ParameterIn.QUERY)
  @Parameter(
      name = "seller",
      description = "Seller mnemonic (RUC).",
      schema = @Schema(type = "string"),
      in = ParameterIn.QUERY)
  @Parameter(
      name = "invoice",
      description = "Invoice reference number.",
      schema = @Schema(type = "string"),
      in = ParameterIn.QUERY)
  public InvoiceDTO searchInvoice(
      @Valid @Parameter(hidden = true) InvoiceSearchParams searchParams) {
    return invoiceService.searchInvoice(searchParams);
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create a single invoice in Trade Innovation.")
  public InvoiceCreatedDTO createInvoice(@Valid @RequestBody InvoiceCreationDTO invoiceDTO) {
    invoiceService.createSingleInvoiceInTi(invoiceDTO);

    return InvoiceCreatedDTO.builder().message("Invoice sent to be created.").build();
  }

  @PostMapping(
      path = "bulk",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create multiple invoices in Trade Innovation.")
  public InvoicesCreatedDTO bulkCreateInvoices(
      @Valid InvoiceBulkCreationForm form, @Valid InvoiceBulkCreationParams params) {
    InvoiceBulkCreationParams.Channel channel =
        Optional.ofNullable(params.channel())
            .map(InvoiceBulkCreationParams.Channel::fromString)
            .orElse(InvoiceBulkCreationParams.Channel.BUSINESS_BANKING);

    if (channel == InvoiceBulkCreationParams.Channel.BUSINESS_BANKING) {
      invoiceBatchOperationsService.createInvoicesInTiWithBusinessBankingChannel(
          form.invoicesFile(), form.batchId());
    } else if (channel == InvoiceBulkCreationParams.Channel.SFTP) {
      invoiceBatchOperationsService.createInvoicesInTIWithSftpChannel(
          form.invoicesFile(), form.batchId());
    }

    return new InvoicesCreatedDTO("Invoices sent to be created.");
  }

  @PostMapping(
      path = "finance",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Finance invoice in Trade Innovation.")
  public InvoiceFinancedDTO financeInvoice(
      @Valid @RequestBody InvoiceFinancingDTO invoiceFinancingDTO) {
    invoiceService.financeInvoice(invoiceFinancingDTO);

    return new InvoiceFinancedDTO("Invoice financing request sent.");
  }

  @PostMapping(path = "report", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(description = "Generate report for payment and collecting pending invoices.")
  @Parameter(
      name = "customerMnemonic",
      example = "1722466420001",
      schema = @Schema(type = "string", minLength = 10, maxLength = 13),
      in = ParameterIn.QUERY)
  @Parameter(
      name = "customerRole",
      description = "Possible values: BUYER or SELLER",
      example = "BUYER",
      schema = @Schema(type = "string"),
      in = ParameterIn.QUERY)
  public ResponseEntity<Resource> generateInvoicesReport(
      @Valid @Parameter(hidden = true) GenerateInvoiceReportParams params) throws IOException {

    Resource resource = null;
    String customerMnemonic = params.customerMnemonic();

    if ("BUYER".equals(params.customerRole())) {
      resource = invoiceBatchOperationsService.generateToPayInvoicesReport(customerMnemonic);
    } else if ("SELLER".equals(params.customerRole())) {
      resource = invoiceBatchOperationsService.generateToCollectInvoicesReport(customerMnemonic);
    }

    // Impossible edge case
    if (resource == null) {
      throw new InvoiceReportException("Could not generate invoices report file.");
    }

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format("attachment; filename=\"%s\"", resource.getFilename()))
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(resource.contentLength())
        .body(resource);
  }
}
