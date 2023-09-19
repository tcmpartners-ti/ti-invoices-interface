package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCreatedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceFinancedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoicesCreatedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Defines the invoices operations.")
@Slf4j
public class InvoiceController {
  private final InvoiceService invoiceService;

  @GetMapping(path = "{invoiceNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get an invoice by its number.")
  public InvoiceDTO getInvoiceByNumber(
    @Valid
    InvoiceSearchParams searchParams,
    @PathVariable
    String invoiceNumber
  ) {
    log.info("Endpoint: /invoices/{}", invoiceNumber);
    return invoiceService.getInvoiceByReference(searchParams, invoiceNumber);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create a single invoice in Trade Innovation.")
  public InvoiceCreatedDTO createInvoice(@Valid @RequestBody InvoiceCreationDTO invoiceDTO) {
    invoiceService.createSingleInvoiceInTi(invoiceDTO);

    return InvoiceCreatedDTO.builder()
      .message("Invoice sent to be created.")
      .build();
  }

  @PostMapping(path = "bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create multiple invoices in Trade Innovation.")
  public InvoicesCreatedDTO bulkCreateInvoices(
    @RequestPart
    @Schema(description = "Invoices file in CSV format.")
    MultipartFile invoicesFile,
    @RequestPart
    @Schema(maxLength = 20, description = "Invoices batch identifier.")
    String batchId
  ) {
    invoiceService.createMultipleInvoicesInTi(invoicesFile, batchId);

    return InvoicesCreatedDTO.builder()
      .message("Invoices sent to be created.")
      .build();
  }

  @PostMapping(path = "finance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Finance invoice in Trade Innovation.")
  public InvoiceFinancedDTO financeInvoice(@Valid @RequestBody InvoiceFinancingDTO invoiceFinancingDTO) {
    invoiceService.financeInvoice(invoiceFinancingDTO);

    return InvoiceFinancedDTO.builder()
      .message("Invoice financing request sent.")
      .build();
  }
}
