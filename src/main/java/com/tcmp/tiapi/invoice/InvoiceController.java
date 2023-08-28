package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCreatedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoicesCreatedDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import io.swagger.v3.oas.annotations.Operation;
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

  private final InvoiceMapper invoiceMapper;

  @GetMapping(path = "{invoiceReference}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get an invoice by its reference.")
  public InvoiceDTO getInvoiceByReference(@PathVariable String invoiceReference) {
    log.info("Endpoint: /invoices/{}", invoiceReference);

    InvoiceMaster invoice = invoiceService.getInvoiceByReference(invoiceReference);

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create a single invoice.")
  public InvoiceCreatedDTO createInvoice(@Valid @RequestBody InvoiceCreationDTO invoiceDTO) {
    CreateInvoiceEventMessage invoice = invoiceMapper.mapDTOToFTIMessage(invoiceDTO);
    invoiceService.sendInvoiceAndGetCorrelationId(invoice);

    return InvoiceCreatedDTO.builder()
      .message("Invoice sent to be created.")
      .build();
  }

  @PostMapping(path = "bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create multiple invoices.")
  public InvoicesCreatedDTO createInvoicesBulk(@RequestParam MultipartFile invoicesFile) {
    invoiceService.createMultipleInvoices(invoicesFile);

    return InvoicesCreatedDTO.builder()
      .message("Invoices sent to be created.")
      .build();
  }
}
