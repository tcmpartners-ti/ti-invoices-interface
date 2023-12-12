package com.tcmp.tiapi.invoice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.InvoiceContextDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
  @Mock private ProducerTemplate producerTemplate;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceEventRepository invoiceEventRepository;
  @Mock private InvoiceMapper invoiceMapper;

  @Captor private ArgumentCaptor<String> routeCaptor;
  @Captor private ArgumentCaptor<CreateInvoiceEventMessage> messageCaptor;

  private InvoiceService testedInvoiceService;

  @BeforeEach
  void setUp() {
    testedInvoiceService =
        new InvoiceService(
            producerTemplate,
            invoiceRepository,
            invoiceEventRepository,
            invoiceMapper,
            new TIServiceRequestWrapper());
  }

  @Test
  void getInvoiceById_itShouldThrowException() {
    Long invoiceId = 1L;

    when(invoiceRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundHttpException.class,
        () -> testedInvoiceService.getInvoiceById(invoiceId),
        String.format("Could not find an invoice with id %s.", invoiceId));
  }

  @Test
  void getInvoiceById_itShouldReturnInvoice() {
    long invoiceId = 1L;

    when(invoiceRepository.findById(anyLong()))
        .thenReturn(Optional.of(InvoiceMaster.builder().id(invoiceId).build()));

    testedInvoiceService.getInvoiceById(invoiceId);

    verify(invoiceMapper).mapEntityToDTO(any(InvoiceMaster.class));
  }

  @Test
  void searchInvoice_itShouldSearchInvoice() {
    InvoiceSearchParams searchParams =
        InvoiceSearchParams.builder()
            .programme("Programme123")
            .seller("Seller123")
            .invoice("Invoice123")
            .build();

    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
            anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(Optional.of(InvoiceMaster.builder().build()));

    testedInvoiceService.searchInvoice(searchParams);

    verify(invoiceMapper).mapEntityToDTO(any(InvoiceMaster.class));
  }

  @Test
  void createSingleInvoiceInTi_itShouldInvokeCamelRouteWhenCreatingInvoice() {
    InvoiceCreationDTO invoiceCreationDTO =
        InvoiceCreationDTO.builder()
            .context(InvoiceContextDTO.builder().theirReference("INV123").customer("C123").build())
            .anchorParty("C123")
            .buyer("C123")
            .invoiceNumber("INV123")
            .faceValue(CurrencyAmountDTO.builder().amount(BigDecimal.TEN).currency("USD").build())
            .outstandingAmount(
                CurrencyAmountDTO.builder().amount(BigDecimal.TEN).currency("USD").build())
            .build();

    String expectedRoute = "direct:mockCreate";

    when(invoiceMapper.mapDTOToFTIMessage(any()))
        .thenReturn(CreateInvoiceEventMessage.builder().invoiceNumber("INV123").build());

    testedInvoiceService.createSingleInvoiceInTi(invoiceCreationDTO);

    verify(producerTemplate).sendBody(routeCaptor.capture(), messageCaptor.capture());
    assertThat(routeCaptor.getValue()).isEqualTo(expectedRoute);
    assertThat(messageCaptor.getValue().getInvoiceNumber())
        .isEqualTo(invoiceCreationDTO.getInvoiceNumber());
  }

  @Test
  void financeInvoice_itShouldSendMessageToTI() {
    String expectedUriFrom = "direct:financeInvoiceInTi";
    FinanceBuyerCentricInvoiceEventMessage expectedMessage =
        FinanceBuyerCentricInvoiceEventMessage.builder().build();

    when(invoiceMapper.mapFinancingDTOToFTIMessage(any(InvoiceFinancingDTO.class)))
        .thenReturn(expectedMessage);

    testedInvoiceService.financeInvoice(InvoiceFinancingDTO.builder().build());

    verify(producerTemplate).sendBody(expectedUriFrom, expectedMessage);
  }
}
