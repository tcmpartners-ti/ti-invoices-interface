package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCreatedDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoicesCreatedDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Defines the invoices operations.")
public class InvoiceController {
  private final InvoiceService invoiceService;

  private final InvoiceMapper invoiceMapper;

  // IMPORTANT: Get operations are already defined.

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create a single invoice.")
  public ResponseEntity<InvoiceCreatedDTO> createInvoice(@Valid @RequestBody InvoiceCreationDTO invoiceDTO) {
    CreateInvoiceEventMessage invoice = invoiceMapper.mapDTOToFTIMessage(invoiceDTO);
    String createdInvoiceUuid = invoiceService.sendAndReceiveInvoiceUUID(invoice);

    return ResponseEntity.ok(InvoiceCreatedDTO.builder()
      .message("Invoice sent to be created.")
      .invoice(new InvoiceCreatedDTO.InvoiceDTO(createdInvoiceUuid))
      .build());
  }

  @PostMapping(value = "bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create multiple invoices.")
  public ResponseEntity<InvoicesCreatedDTO> createInvoicesBulk(@RequestParam MultipartFile invoicesFile) {
    invoiceService.createMultipleInvoices(invoicesFile);

    return ResponseEntity.ok(InvoicesCreatedDTO.builder()
      .message("Invoices have been sent to be created.")
      .build());
  }
}
