package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.BadRequestHttpException;
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
  @Mock private CounterPartyRepository counterPartyRepository;
  @Mock private ProgramRepository programRepository;
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
      counterPartyRepository,
      programRepository,
      invoiceMapper
    );
  }

  @Test
  void itShouldGetInvoiceByReference() {
    String invoiceReference = "INV123";
    Long expectedBuyerId = 1L;
    Long expectedSellerId = 2L;
    Long expectedProgramId = 1L;

    when(invoiceRepository.findFirstByReference(invoiceReference))
      .thenReturn(Optional.of(InvoiceMaster.builder()
        .id(1L)
        .buyerId(1L)
        .sellerId(1L)
        .programmeId(1L)
        .reference(invoiceReference)
        .build()));
    when(counterPartyRepository.findById(anyLong()))
      .thenReturn(Optional.of(CounterParty.builder()
        .id(expectedBuyerId)
        .build()));
    when(counterPartyRepository.findById(anyLong()))
      .thenReturn(Optional.of(CounterParty.builder()
        .id(expectedSellerId)
        .build()));
    when(programRepository.findByPk(anyLong()))
      .thenReturn(Optional.of(Program.builder()
        .pk(expectedProgramId)
        .build()));

    testedInvoiceService.getInvoiceByReference(invoiceReference);

    verify(invoiceRepository).findFirstByReference(invoiceReference);
    verify(invoiceMapper).mapEntityToDTO(
      any(InvoiceMaster.class),
      any(CounterParty.class),
      any(CounterParty.class),
      any(Program.class)
    );
  }

  @Test
  void itShouldThrowExceptionWhenInvoiceNotFoundByReference() {
    String invoiceReference = "INV123";

    when(invoiceRepository.findFirstByReference(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedInvoiceService.getInvoiceByReference(invoiceReference),
      String.format("Could not find an invoice with reference %s.", invoiceReference));
  }

  @Test
  void itShouldInvokeCamelRouteWhenCreatingInvoice() {
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

    verify(producerTemplate).sendBodyAndHeaders(
      routeCaptor.capture(),
      messageCaptor.capture(),
      headersCaptor.capture()
    );
    assertThat(routeCaptor.getValue()).isEqualTo(expectedRoute);
    assertThat(messageCaptor.getValue().getInvoiceNumber()).isEqualTo(invoiceCreationDTO.getInvoiceNumber());
    assertThat(headersCaptor.getValue().get("JMSCorrelationID")).isNotNull();
  }


  @Test
  void itShouldSendToCamelRouteIfFileIsCorrect() throws IOException {
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
  void itShouldSetBatchIdHeader() throws IOException {
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
  void itShouldThrowExceptionWhenFileIsEmpty() {
    MultipartFile multipartFile = new MockMultipartFile("invoices.csv", new byte[0]);

    assertThrows(InvalidFileHttpException.class,
      () -> testedInvoiceService.createMultipleInvoicesInTi(multipartFile, "123"));
  }

  @Test
  void itShouldThrowExceptionWhenInvalidFile() throws IOException {
    MultipartFile problematicFile = mock(MultipartFile.class);

    when(problematicFile.getInputStream())
      .thenThrow(new IOException("Simulated read error"));

    assertThrows(InvalidFileHttpException.class,
      () -> testedInvoiceService.createMultipleInvoicesInTi(problematicFile, "123"));
  }

  @Test
  void itShouldThrowExceptionWhenBatchIdTooLong() {
    String lengthExceedingBatchId = "XXXXXXXXXXXXXXXXXXXXX";
    MultipartFile mockMultipartFile = mock(MultipartFile.class);

    assertThrows(BadRequestHttpException.class, () ->
      testedInvoiceService.createMultipleInvoicesInTi(mockMultipartFile, lengthExceedingBatchId));
  }
}
