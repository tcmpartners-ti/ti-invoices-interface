package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.InvoiceConfiguration;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
  @Mock private ProducerTemplate producerTemplate;
  @Mock private InvoiceConfiguration invoiceConfiguration;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceMapper invoiceMapper;

  @Captor private ArgumentCaptor<String> routeCaptor;
  @Captor private ArgumentCaptor<CreateInvoiceEventMessage> messageCaptor;
  @Captor private ArgumentCaptor<Map<String, Object>> headersCaptor;
  @Captor private ArgumentCaptor<BufferedReader> bufferedReaderCaptor;

  private InvoiceService testedInvoiceService;

  @BeforeEach
  void setUp() {
    testedInvoiceService = new InvoiceService(
      producerTemplate,
      invoiceConfiguration,
      invoiceRepository,
      invoiceMapper
    );
  }

  @Test
  void getInvoiceById_itShouldThrowException() {
    Long invoiceId = 1L;

    when(invoiceRepository.findById(anyLong()))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedInvoiceService.getInvoiceById(invoiceId),
      String.format("Could not find an invoice with id %s.", invoiceId));
  }

  @Test
  void getInvoiceById_itShouldReturnInvoice() {
    long invoiceId = 1L;

    when(invoiceRepository.findById(anyLong()))
      .thenReturn(Optional.of(
        InvoiceMaster.builder()
          .id(invoiceId)
          .build()
      ));

    testedInvoiceService.getInvoiceById(invoiceId);

    verify(invoiceMapper).mapEntityToDTO(any(InvoiceMaster.class));
  }

  @Test
  void searchInvoice_itShouldSearchInvoice() {
    InvoiceSearchParams searchParams = InvoiceSearchParams.builder()
      .programme("Programme123")
      .seller("Seller123")
      .invoice("Invoice123")
      .build();

    when(invoiceRepository.findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
      anyString(),
      anyString(),
      anyString(),
      anyBoolean())
    )
      .thenReturn(Optional.of(InvoiceMaster.builder().build()));

    testedInvoiceService.searchInvoice(searchParams);

    verify(invoiceMapper).mapEntityToDTO(any(InvoiceMaster.class));
  }

  @Test
  void createSingleInvoiceInTi_itShouldInvokeCamelRouteWhenCreatingInvoice() {
    InvoiceCreationDTO invoiceCreationDTO = InvoiceCreationDTO.builder()
      .invoiceNumber("INV123")
      .build();

    String expectedRoute = "direct:mockCreate";

    when(invoiceConfiguration.getUriCreateFrom())
      .thenReturn(expectedRoute);
    when(invoiceMapper.mapDTOToFTIMessage(any()))
      .thenReturn(CreateInvoiceEventMessage.builder()
        .invoiceNumber("INV123")
        .build());

    testedInvoiceService.createSingleInvoiceInTi(invoiceCreationDTO);

    verify(producerTemplate).sendBody(
      routeCaptor.capture(),
      messageCaptor.capture()
    );
    assertThat(routeCaptor.getValue()).isEqualTo(expectedRoute);
    assertThat(messageCaptor.getValue().getInvoiceNumber()).isEqualTo(invoiceCreationDTO.getInvoiceNumber());
  }


  @Test
  void createMultipleInvoicesInTi_itShouldSendToCamelRouteIfFileIsCorrect() throws IOException {
    String fileContent = "A,B,C";
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    MultipartFile multipartFile = new MockMultipartFile("invoices.csv", inputStream);
    String expectedRoute = "direct:createBulkInvoices";

    when(invoiceConfiguration.getUriBulkCreateFrom())
      .thenReturn(expectedRoute);

    testedInvoiceService.createMultipleInvoicesInTi(multipartFile, "123");

    verify(producerTemplate).sendBodyAndHeaders(
      routeCaptor.capture(),
      bufferedReaderCaptor.capture(),
      any()
    );
    assertThat(routeCaptor.getValue()).isEqualTo(expectedRoute);
  }

  @Test
  void createMultipleInvoicesInTi_itShouldSetBatchIdHeader() throws IOException {
    String fileContent = "A,B,C";
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    MultipartFile multipartFile = new MockMultipartFile("invoices.csv", inputStream);
    String expectedRoute = "direct:createBulkInvoices";

    when(invoiceConfiguration.getUriBulkCreateFrom())
      .thenReturn(expectedRoute);

    testedInvoiceService.createMultipleInvoicesInTi(multipartFile, "123");

    verify(producerTemplate).sendBodyAndHeaders(
      anyString(),
      any(BufferedReader.class),
      headersCaptor.capture()
    );

    assertThat(headersCaptor.getValue().get("batchId")).isNotNull();
  }

  @Test
  void createMultipleInvoicesInTi_itShouldThrowExceptionWhenFileIsEmpty() {
    MultipartFile multipartFile = new MockMultipartFile("invoices.csv", new byte[0]);

    assertThrows(InvalidFileHttpException.class,
      () -> testedInvoiceService.createMultipleInvoicesInTi(multipartFile, "123"));
  }

  @Test
  void createMultipleInvoicesInTi_itShouldThrowExceptionWhenInvalidFile() throws IOException {
    MultipartFile problematicFile = mock(MultipartFile.class);

    when(problematicFile.getInputStream())
      .thenThrow(new IOException("Simulated read error"));

    assertThrows(InvalidFileHttpException.class,
      () -> testedInvoiceService.createMultipleInvoicesInTi(problematicFile, "123"));
  }

  @Test
  void financeInvoice_itShouldSendMessageToTI() {
    String expectedUriFrom = "direct:financeInvoiceInTi";
    FinanceBuyerCentricInvoiceEventMessage expectedMessage = FinanceBuyerCentricInvoiceEventMessage.builder()
      .build();

    when(invoiceConfiguration.getUriFinanceFrom())
      .thenReturn(expectedUriFrom);
    when(invoiceMapper.mapFinancingDTOToFTIMessage(any(InvoiceFinancingDTO.class)))
      .thenReturn(expectedMessage);

    testedInvoiceService.financeInvoice(InvoiceFinancingDTO.builder().build());

    verify(producerTemplate).sendBody(expectedUriFrom, expectedMessage);
  }
}
