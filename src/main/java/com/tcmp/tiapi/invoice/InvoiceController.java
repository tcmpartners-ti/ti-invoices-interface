package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceBulkCreationForm;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCreatedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceFinancedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoicesCreatedDTO;
import com.tcmp.tiapi.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Defines the invoices operations.")
@Slf4j
public class InvoiceController {
  private final InvoiceService invoiceService;

  @GetMapping(path = "{invoiceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get an invoice by its internal id.")
  public InvoiceDTO getInvoiceById(@PathVariable Long invoiceId) {
    return invoiceService.getInvoiceById(invoiceId);
  }

  @GetMapping(path = "search", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Search an invoice by its programme, seller and number.")
  @Parameter(
      name = "programme",
      description = "Indicates the credit line to which the invoice relates.",
      in = ParameterIn.QUERY)
  @Parameter(name = "seller", description = "Seller mnemonic (RUC).", in = ParameterIn.QUERY)
  @Parameter(name = "invoice", description = "Invoice reference number.", in = ParameterIn.QUERY)
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
  public InvoicesCreatedDTO bulkCreateInvoices(@Valid InvoiceBulkCreationForm form) {
    invoiceService.createMultipleInvoicesInTi(form.invoicesFile(), form.batchId());

    return InvoicesCreatedDTO.builder().message("Invoices sent to be created.").build();
  }

  @PostMapping(
      path = "finance",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Finance invoice in Trade Innovation.")
  public InvoiceFinancedDTO financeInvoice(
      @Valid @RequestBody InvoiceFinancingDTO invoiceFinancingDTO) {
    invoiceService.financeInvoice(invoiceFinancingDTO);

    return InvoiceFinancedDTO.builder().message("Invoice financing request sent.").build();
  }
}
