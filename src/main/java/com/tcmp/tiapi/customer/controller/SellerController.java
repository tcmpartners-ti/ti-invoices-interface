package com.tcmp.tiapi.customer.controller;

import com.tcmp.tiapi.customer.service.SellerService;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sellers")
@RequiredArgsConstructor
@Tag(name = "Sellers", description = "Defines the sellers operations.")
public class SellerController {
  private final SellerService sellerService;

  @GetMapping(path = "{sellerMnemonic}/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get seller's invoices by its mnemonic (ruc).")
  public PaginatedResult<InvoiceDTO> getSellerInvoicesByMnemonic(
    PageParams pageParams,
    @PathVariable String sellerMnemonic
  ) {
    return sellerService.getSellerInvoices(sellerMnemonic, pageParams);
  }
}
