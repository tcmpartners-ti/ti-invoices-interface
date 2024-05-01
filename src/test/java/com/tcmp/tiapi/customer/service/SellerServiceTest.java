package com.tcmp.tiapi.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {
  @Mock private CustomerRepository customerRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ProgramRepository programRepository;
  @Mock private InvoiceMapper invoiceMapper;
  @Spy private ProgramMapper programMapper = Mappers.getMapper(ProgramMapper.class);
  private final CurrencyAmountMapper currencyAmountMapper =
      Mappers.getMapper(CurrencyAmountMapper.class);

  @InjectMocks private SellerService sellerService;

  @BeforeEach
  void setUp() {
    var mockedToday = LocalDate.of(2024, 2, 8);
    var mockedClock =
        Clock.fixed(
            mockedToday.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    ReflectionTestUtils.setField(sellerService, "clock", mockedClock);
    ReflectionTestUtils.setField(programMapper, "currencyAmountMapper", currencyAmountMapper);
  }

  @Test
  void getSellerInvoices_itShouldThrowExceptionIfNoInvoicesAreFound() {
    String sellerMnemonic = "1722466421001";
    SearchSellerInvoicesParams searchParams =
        SearchSellerInvoicesParams.builder().status("O").build();
    PageParams pageParams = new PageParams();

    when(customerRepository.existsByIdMnemonic(sellerMnemonic)).thenReturn(false);

    assertThrows(
        NotFoundHttpException.class,
        () -> sellerService.getSellerInvoices(sellerMnemonic, searchParams, pageParams));
  }

  @Test
  void getSellerInvoices_itShouldReturnEmptyListIfNoInvoicesAreFoundForStatus() {
    String sellerMnemonic = "1722466421001";
    SearchSellerInvoicesParams searchParams =
        SearchSellerInvoicesParams.builder().status("O").build();
    PageParams pageParams = new PageParams();

    when(customerRepository.existsByIdMnemonic(sellerMnemonic)).thenReturn(true);
    when(invoiceRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    var actualInvoicesPage =
        sellerService.getSellerInvoices(sellerMnemonic, searchParams, pageParams);

    var expectedInvoicesPageSize = 0;

    assertEquals(expectedInvoicesPageSize, actualInvoicesPage.getData().size());
  }

  @Test
  void getSellerInvoices_itShouldReturnSellerInvoices() {
    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(true);
    when(invoiceRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(InvoiceMaster.builder().build())));

    var invoicesPage =
        sellerService.getSellerInvoices(
            "", SearchSellerInvoicesParams.builder().status("O").build(), new PageParams());

    assertNotNull(invoicesPage);
    verify(invoiceMapper).mapEntitiesToDTOs(any());
  }

  @Test
  void getSellerProgramsByMnemonic_itShouldThrowNotFoundException() {
    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(false);

    var pageParams = new PageParams();
    assertThrows(
        NotFoundHttpException.class,
        () -> sellerService.getSellerProgramsByMnemonic("123", pageParams));
  }

  @Test
  void getSellerProgramsByMnemonic_itShouldReturnPrograms() {
    var programs =
        List.of(
            Program.builder().id("Program1").customerMnemonic("1722466420").build(),
            Program.builder().id("Program2").customerMnemonic("1722466420").build());

    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(true);
    when(programRepository.findAllBySellerMnemonic(anyString(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(programs));

    var actualProgramsPage = sellerService.getSellerProgramsByMnemonic("123", new PageParams());

    verify(programRepository).findAllBySellerMnemonic(eq("123"), any(Pageable.class));

    assertEquals("Program1", actualProgramsPage.getData().get(0).getId());
    assertEquals("Program2", actualProgramsPage.getData().get(1).getId());
  }

  @Test
  void getSellerProgramsByCif_itShouldThrowNofFoundException() {
    when(customerRepository.existsByNumber(anyString())).thenReturn(false);

    var pageParams = new PageParams();
    assertThrows(
        NotFoundHttpException.class, () -> sellerService.getSellerProgramsByCif("123", pageParams));
  }

  @Test
  void getSellerProgramsByCif_itShouldReturnProgrammes() {
    var programs =
        List.of(
            Program.builder().id("Program1").customerMnemonic("1722466420").build(),
            Program.builder().id("Program2").customerMnemonic("1722466420").build());

    when(customerRepository.existsByNumber(anyString())).thenReturn(true);
    when(programRepository.findAllBySellerCif(anyString(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(programs));

    var actualProgramsPage = sellerService.getSellerProgramsByCif("123", new PageParams());

    verify(programRepository).findAllBySellerCif(eq("123"), any(Pageable.class));

    assertEquals("Program1", actualProgramsPage.getData().get(0).getId());
    assertEquals("Program2", actualProgramsPage.getData().get(1).getId());
  }

  @Test
  void getSellerOutstandingBalanceByMnemonic_itShouldThrowExceptionIfCustomerNotFound() {
    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(false);

    assertThrows(
        NotFoundHttpException.class,
        () -> sellerService.getSellerOutstandingBalanceByMnemonic("1722466420001", null));
  }

  @Test
  void getSellerOutstandingBalanceByMnemonic_itShouldThrowExceptionIfBuyerNotFound() {
    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(true).thenReturn(false);

    assertThrows(
        NotFoundHttpException.class,
        () ->
            sellerService.getSellerOutstandingBalanceByMnemonic("1722466420001", "1722466420002"));
  }

  @Test
  void
      getSellerOutstandingBalanceByMnemonic_itShouldThrowExceptionIfSellerHasNoLinkedInvoicesToBuyer() {
    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(true);

    assertThrows(
        NotFoundHttpException.class,
        () ->
            sellerService.getSellerOutstandingBalanceByMnemonic("1722466420001", "1722466420002"));
  }

  @Test
  void getSellerOutstandingBalanceByMnemonic_itShouldReturnOutstandingBalance() {
    when(customerRepository.existsByIdMnemonic(anyString())).thenReturn(true);
    when(invoiceRepository.getNotFinancedOutstandingBalanceBySellerMnemonic(anyString(), any()))
        .thenReturn(Optional.of(BigDecimal.valueOf(100000L)));
    when(invoiceRepository.getFinancedOutstandingBalanceBySellerMnemonic(anyString(), any()))
        .thenReturn(Optional.of(BigDecimal.valueOf(50000L)));

    var actualBalanceDto = sellerService.getSellerOutstandingBalanceByMnemonic("", null);

    var expectedBalance = new BigDecimal("1500.00");
    assertEquals(expectedBalance, actualBalanceDto.balance());
  }
}
