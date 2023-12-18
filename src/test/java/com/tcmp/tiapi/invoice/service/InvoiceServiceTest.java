package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.*;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import java.lang.reflect.Field;
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

  @Captor private ArgumentCaptor<ServiceRequest<?>> messageCaptor;

  private InvoiceService invoiceService;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    invoiceService =
        new InvoiceService(
            producerTemplate,
            invoiceRepository,
            invoiceEventRepository,
            invoiceMapper,
            new TIServiceRequestWrapper());

    Field uriFromFtiOutgoing = InvoiceService.class.getDeclaredField("uriFromFtiOutgoing");
    uriFromFtiOutgoing.setAccessible(true);
    uriFromFtiOutgoing.set(invoiceService, "direct:mock");
  }

  @Test
  void getInvoiceById_itShouldThrowException() {
    Long invoiceId = 1L;

    when(invoiceRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundHttpException.class,
        () -> invoiceService.getInvoiceById(invoiceId),
        String.format("Could not find an invoice with id %s.", invoiceId));
  }

  @Test
  void getInvoiceById_itShouldReturnInvoice() {
    long invoiceId = 1L;

    when(invoiceRepository.findById(anyLong()))
        .thenReturn(Optional.of(InvoiceMaster.builder().id(invoiceId).build()));

    invoiceService.getInvoiceById(invoiceId);

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

    invoiceService.searchInvoice(searchParams);

    verify(invoiceMapper).mapEntityToDTO(any(InvoiceMaster.class));
  }

  @Test
  void searchInvoice_itShouldThrowNotFoundException() {
    InvoiceSearchParams nonExistentInvoiceParams =
        InvoiceSearchParams.builder()
            .programme("SteelBallRun")
            .seller("Funny Valentine")
            .invoice("001-001-d4c")
            .build();

    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
            anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundHttpException.class, () -> invoiceService.searchInvoice(nonExistentInvoiceParams));
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

    when(invoiceMapper.mapDTOToFTIMessage(any()))
        .thenReturn(CreateInvoiceEventMessage.builder().invoiceNumber("INV123").build());

    invoiceService.createSingleInvoiceInTi(invoiceCreationDTO);

    verify(invoiceEventRepository).save(any(InvoiceEventInfo.class));
    verify(producerTemplate).sendBody(anyString(), messageCaptor.capture());
    assertEquals(
        invoiceCreationDTO.getInvoiceNumber(),
        ((CreateInvoiceEventMessage) messageCaptor.getValue().getBody()).getInvoiceNumber());
  }

  @Test
  void financeInvoice_itThrowNotFoundException() {
    InvoiceFinancingDTO invoiceFinancingDto =
        InvoiceFinancingDTO.builder()
            .programme("SteelBallRun")
            .seller("D4C")
            .invoice(InvoiceNumberDTO.builder().number("123").build())
            .build();

    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReference(
            anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundHttpException.class, () -> invoiceService.financeInvoice(invoiceFinancingDto));
  }

  @Test
  void financeInvoice_itShouldSendMessageToTI() {
    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReference(
            anyString(), anyString(), anyString()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("123").build()));
    when(invoiceMapper.mapFinancingDTOToFTIMessage(any(InvoiceFinancingDTO.class)))
        .thenReturn(FinanceBuyerCentricInvoiceEventMessage.builder().build());

    invoiceService.financeInvoice(
        InvoiceFinancingDTO.builder()
            .invoice(InvoiceNumberDTO.builder().number("001-001-123").build())
            .programme("Program123")
            .seller("1722466420")
            .build());

    verify(invoiceEventRepository).save(any(InvoiceEventInfo.class));
    verify(producerTemplate).sendBody(anyString(), any(ServiceRequest.class));
  }
}
