package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.InvoiceRepository;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class SellerServiceTest {

  @Mock
  private CounterPartyRepository counterPartyRepository;
  @Mock
  private InvoiceRepository invoiceRepository;
  @Mock
  private ProgramRepository programRepository;
  @Mock
  private InvoiceMapper invoiceMapper;

  private SellerService sellerService;

  @BeforeEach
  public void setup() {
    openMocks(this);

    sellerService = new SellerService(
      counterPartyRepository,
      invoiceRepository,
      programRepository,
      invoiceMapper
    );
  }

  @Test
  void shouldGetSellerInvoices() {
    // Setup
    String sellerMnemonic = "seller";
    PageParams pageParams = new PageParams();
    List<CounterParty> counterParties = Collections.singletonList(new CounterParty());
    List<InvoiceMaster> invoiceMasters = Collections.singletonList(new InvoiceMaster());

    when(counterPartyRepository.findByCustomerMnemonicAndRole(any(), any())).thenReturn(counterParties);
    when(invoiceRepository.findBySellerIdIn(anyList(), any())).thenReturn(new PageImpl<>(invoiceMasters));
    when(invoiceRepository.findBySellerIdIn(anyList(), any())).thenReturn(new PageImpl<>(invoiceMasters));
    when(counterPartyRepository.findByIdIn(anyList())).thenReturn(counterParties);
    when(programRepository.findByPkIn(anyList())).thenReturn(Collections.singletonList(new Program()));
    when(invoiceMapper.mapEntityToDTO(any(), any(), any(), any())).thenReturn(InvoiceDTO.builder().build());

    // Execution
    PaginatedResult<InvoiceDTO> result = sellerService.getSellerInvoices(sellerMnemonic, pageParams);

    // Assertions
    assertNotNull(result);
    assertFalse(result.getData().isEmpty());
    assertNotNull(result.getMeta());
    assertTrue(result.getMeta().containsKey("pagination"));
  }
}
