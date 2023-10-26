package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {
  @Mock private CustomerRepository customerRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceMapper invoiceMapper;

  private SellerService sellerService;

  @BeforeEach
  public void setup() {
    sellerService = new SellerService(
      customerRepository,
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

    when(customerRepository.existsByIdMnemonic(sellerMnemonic))
      .thenReturn(false);

    assertThrows(NotFoundHttpException.class, () ->
      sellerService.getSellerInvoices(sellerMnemonic, searchParams, pageParams));
  }
}
