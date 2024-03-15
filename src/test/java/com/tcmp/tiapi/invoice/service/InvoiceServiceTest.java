package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.*;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.InvoiceNumbers;
import com.tcmp.tiapi.invoice.dto.ti.finance.InvoiceNumbersContainer;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.ReplyFormat;
import com.tcmp.tiapi.ti.dto.request.RequestHeader;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
  @Mock private ProducerTemplate producerTemplate;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceEventRepository invoiceEventRepository;
  @Mock private InvoiceMapper invoiceMapper;
  @Mock private TIServiceRequestWrapper serviceRequestWrapper;
  @Mock private UUIDGenerator uuidGenerator;

  @Captor private ArgumentCaptor<ServiceRequest<?>> messageCaptor;
  @Captor private ArgumentCaptor<InvoiceEventInfo> invoiceInfoArgumentCaptor;

  @InjectMocks private InvoiceService invoiceService;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
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
    var invoiceUuid = "001-001";
    var invoiceCreationDTO =
        InvoiceCreationDTO.builder()
            .context(InvoiceContextDTO.builder().theirReference("INV123").customer("C123").build())
            .anchorParty("C123")
            .buyer("C123")
            .invoiceNumber("INV123")
            .faceValue(CurrencyAmountDTO.builder().amount(BigDecimal.TEN).currency("USD").build())
            .outstandingAmount(
                CurrencyAmountDTO.builder().amount(BigDecimal.TEN).currency("USD").build())
            .build();

    when(invoiceMapper.mapDTOToFTIMessage(any(InvoiceCreationDTO.class)))
        .thenReturn(CreateInvoiceEventMessage.builder().invoiceNumber("INV123").build());
    when(serviceRequestWrapper.wrapRequest(any(TIService.class), any(), any(), any(), any()))
        .thenReturn(
            ServiceRequest.builder()
                .header(RequestHeader.builder().correlationId(invoiceUuid).build())
                .body(CreateInvoiceEventMessage.builder().invoiceNumber("INV123").build())
                .build());
    when(uuidGenerator.getNewId()).thenReturn(invoiceUuid);

    invoiceService.createSingleInvoiceInTi(invoiceCreationDTO);

    verify(invoiceEventRepository).save(invoiceInfoArgumentCaptor.capture());
    verify(serviceRequestWrapper)
        .wrapRequest(
            eq(TIService.TRADE_INNOVATION),
            eq(TIOperation.CREATE_INVOICE),
            eq(ReplyFormat.STATUS),
            anyString(),
            any(CreateInvoiceEventMessage.class));
    verify(producerTemplate).sendBody(anyString(), messageCaptor.capture());

    assertEquals(
        invoiceCreationDTO.getInvoiceNumber(),
        ((CreateInvoiceEventMessage) messageCaptor.getValue().getBody()).getInvoiceNumber());
    assertEquals(invoiceUuid, invoiceInfoArgumentCaptor.getValue().getId());
    assertEquals(invoiceUuid, messageCaptor.getValue().getHeader().getCorrelationId());
  }

  @Test
  void financeInvoice_itShouldThrowNotFoundException() {
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
    var invoiceUuid = "000-001";
    var mappedMessage =
        FinanceBuyerCentricInvoiceEventMessage.builder()
            .invoiceNumbersContainer(
                new InvoiceNumbersContainer(
                    List.of(InvoiceNumbers.builder().invoiceNumber("001-001-123").build())))
            .build();

    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReference(
            anyString(), anyString(), anyString()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("123").build()));
    when(invoiceMapper.mapFinancingDTOToFTIMessage(any(InvoiceFinancingDTO.class)))
        .thenReturn(mappedMessage);
    when(serviceRequestWrapper.wrapRequest(any(TIService.class), any(), any(), any(), any()))
        .thenReturn(
            ServiceRequest.builder()
                .header(RequestHeader.builder().correlationId(invoiceUuid).build())
                .body(mappedMessage)
                .build());
    when(uuidGenerator.getNewId()).thenReturn(invoiceUuid);

    invoiceService.financeInvoice(
        InvoiceFinancingDTO.builder()
            .invoice(InvoiceNumberDTO.builder().number("001-001-123").build())
            .programme("Program123")
            .seller("1722466420")
            .build());

    verify(invoiceEventRepository).save(invoiceInfoArgumentCaptor.capture());
    verify(serviceRequestWrapper)
        .wrapRequest(
            eq(TIService.TRADE_INNOVATION),
            eq(TIOperation.FINANCE_INVOICE),
            eq(ReplyFormat.STATUS),
            anyString(),
            any(FinanceBuyerCentricInvoiceEventMessage.class));
    verify(producerTemplate).sendBody(anyString(), messageCaptor.capture());

    assertEquals(invoiceUuid, invoiceInfoArgumentCaptor.getValue().getId());
    assertEquals(invoiceUuid, messageCaptor.getValue().getHeader().getCorrelationId());
  }
}
