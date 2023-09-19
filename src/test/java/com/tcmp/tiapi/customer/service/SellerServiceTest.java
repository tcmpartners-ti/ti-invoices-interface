package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

  @Mock private CounterPartyRepository counterPartyRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ProgramRepository programRepository;
  @Mock private InvoiceMapper invoiceMapper;

  @Captor private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

  private SellerService sellerService;

  @BeforeEach
  public void setup() {
    sellerService = new SellerService(
      counterPartyRepository,
      invoiceRepository,
      programRepository,
      invoiceMapper
    );
  }

  @Test
  void getSellerInvoices_itShouldThrowExceptionIfNoInvoicesAreFound() {
    String expectedSellerMnemonic = "1722466421001";
    SearchSellerInvoicesParams expectedSearchParams = SearchSellerInvoicesParams.builder().build();
    PageParams expectedPageParams = new PageParams();

    when(counterPartyRepository.findUniqueIdsByCustomerMnemonicAndRole(anyString(), anyChar()))
      .thenReturn(List.of());

    assertThrows(NotFoundHttpException.class, () ->
      sellerService.getSellerInvoices(expectedSellerMnemonic, expectedSearchParams, expectedPageParams));
  }

  @Test
  void getSellerInvoices_itShouldCallInvoiceRepositoryWithCorrectArguments() {
    String expectedSellerMnemonic = "1722466421001";
    List<Long> expectedSellerIds = List.of(1L, 2L);
    List<InvoiceMaster> expectedInvoices = List.of(
      InvoiceMaster.builder()
        .id(1L)
        .sellerId(1L)
        .build(),
      InvoiceMaster.builder()
        .id(2L)
        .sellerId(2L)
        .build()
    );
    PageParams expectedPageParams = new PageParams();

    when(counterPartyRepository.findUniqueIdsByCustomerMnemonicAndRole(anyString(), anyChar()))
      .thenReturn(expectedSellerIds);
    when(invoiceRepository.findAll(any(Specification.class), any(PageRequest.class)))
      .thenReturn(new PageImpl<>(expectedInvoices));

    sellerService.getSellerInvoices(
      expectedSellerMnemonic,
      SearchSellerInvoicesParams.builder().build(),
      new PageParams()
    );

    verify(invoiceRepository)
      .findAll(any(Specification.class), pageRequestArgumentCaptor.capture());
    assertEquals(expectedPageParams.getPage(), pageRequestArgumentCaptor.getValue().getPageNumber());
    assertEquals(expectedPageParams.getSize(), pageRequestArgumentCaptor.getValue().getPageSize());
  }
}
