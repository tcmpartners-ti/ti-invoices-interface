package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceSpecifications;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {
  private final InvoiceRepository invoiceRepository;
  private final InvoiceMapper invoiceMapper;

  public PaginatedResult<InvoiceDTO> getSellerInvoices(
    String sellerMnemonic,
    SearchSellerInvoicesParams searchParams,
    PageParams pageParams
  ) {
    Page<InvoiceMaster> sellerInvoicesPage = invoiceRepository.findAll(
      InvoiceSpecifications.filterBySellerMnemonicAndStatus(sellerMnemonic, searchParams.status()),
      PageRequest.of(pageParams.getPage(), pageParams.getSize())
    );

    if (sellerInvoicesPage.isEmpty()) {
      throw new NotFoundHttpException(
        String.format(
          "Could not find invoices for seller with mnemonic %s and status %s.",
          sellerMnemonic,
          searchParams.status()
        ));
    }

    List<InvoiceDTO> invoicesDTOs = invoiceMapper.mapEntitiesToDTOs(sellerInvoicesPage.getContent());

    return PaginatedResult.<InvoiceDTO>builder()
      .data(invoicesDTOs)
      .meta(PaginatedResultMeta.builder()
        .isLastPage(sellerInvoicesPage.isLast())
        .totalPages(sellerInvoicesPage.getTotalPages())
        .totalItems(sellerInvoicesPage.getTotalElements())
        .build())
      .build();
  }
}
