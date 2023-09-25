package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceMapper invoiceMapper;

  private SellerService sellerService;

  @BeforeEach
  public void setup() {
    sellerService = new SellerService(
      invoiceRepository,
      invoiceMapper
    );
  }

  @Test
  void getSellerInvoices_itShouldThrowExceptionIfNoInvoicesAreFound() {
    String sellerMnemonic = "1722466421001";
    SearchSellerInvoicesParams searchParams = SearchSellerInvoicesParams.builder()
      .status("O")
      .build();
    PageParams pageParams = new PageParams();

    when(invoiceRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(Page.empty());

    assertThrows(NotFoundHttpException.class, () ->
      sellerService.getSellerInvoices(sellerMnemonic, searchParams, pageParams));
  }
}
