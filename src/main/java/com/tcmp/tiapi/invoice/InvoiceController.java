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

  @GetMapping(path = "{invoiceNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get an invoice by its number.")
  public InvoiceDTO getInvoiceByNumber(@PathVariable String invoiceNumber) {
    log.info("Endpoint: /invoices/{}", invoiceNumber);

    InvoiceMaster invoice = invoiceService.getInvoiceByReference(invoiceNumber);

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create a single invoice in Trade Innovation.")
  public InvoiceCreatedDTO createInvoice(@Valid @RequestBody InvoiceCreationDTO invoiceDTO) {
    CreateInvoiceEventMessage invoice = invoiceMapper.mapDTOToFTIMessage(invoiceDTO);
    invoiceService.sendInvoiceAndGetCorrelationId(invoice);

    return InvoiceCreatedDTO.builder()
      .message("Invoice sent to be created.")
      .build();
  }

  @PostMapping(path = "bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create multiple invoices in Trade Innovation.")
  public InvoicesCreatedDTO bulkCreateInvoices(@RequestPart MultipartFile invoicesFile, @RequestPart String batchId) {
    invoiceService.createMultipleInvoices(invoicesFile, batchId);

    return InvoicesCreatedDTO.builder()
      .message("Invoices sent to be created.")
      .build();
  }
}
