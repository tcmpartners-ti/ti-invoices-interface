package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.BadRequestHttpException;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
  @Disabled("This feature will be reviewed.")
  void getInvoiceByReference_itShouldGetInvoiceByReference() {
    String invoiceReference = "INV123";
    Long expectedBuyerId = 1L;
    Long expectedSellerId = 2L;
    Long expectedProgramId = 1L;
    InvoiceSearchParams expectedInvoiceSearchParams = InvoiceSearchParams.builder()
      .program(1L)
      .seller(2L)
      .build();

    when(invoiceRepository.findFirstByProgrammeIdAndSellerIdAndReference(anyLong(), anyLong(), anyString()))
      .thenReturn(Optional.of(InvoiceMaster.builder()
        .id(1L)
        .buyerId(1L)
        .sellerId(1L)
        .programmeId(1L)
        .reference(invoiceReference)
        .build()));
    when(counterPartyRepository.findById(anyLong()))
      .thenReturn(Optional.of(CounterParty.builder().id(expectedBuyerId).build()));
    when(counterPartyRepository.findById(anyLong()))
      .thenReturn(Optional.of(CounterParty.builder().id(expectedSellerId).build()));
    when(programRepository.findByPk(anyLong()))
      .thenReturn(Optional.of(Program.builder().pk(expectedProgramId).build()));

    testedInvoiceService.getInvoiceByReference(expectedInvoiceSearchParams, invoiceReference);

    verify(invoiceRepository).findFirstByProgrammeIdAndSellerIdAndReference(expectedProgramId, expectedSellerId, invoiceReference);
    verify(invoiceMapper).mapEntityToDTO(
      any(InvoiceMaster.class),
      any(CounterParty.class),
      any(CounterParty.class),
      any(Program.class)
    );
  }

  @Test
  void getInvoiceByReference_itShouldThrowExceptionWhenInvoiceNotFoundByReference() {
    String invoiceReference = "INV123";
    InvoiceSearchParams expectedInvoiceSearchParams = InvoiceSearchParams.builder()
      .program(1L)
      .seller(1L)
      .build();

    when(invoiceRepository.findFirstByProgrammeIdAndSellerIdAndReference(anyLong(), anyLong(), anyString()))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedInvoiceService.getInvoiceByReference(expectedInvoiceSearchParams, invoiceReference),
      String.format("Could not find an invoice with reference %s.", invoiceReference));
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
  void createMultipleInvoicesInTi_itShouldThrowExceptionWhenBatchIdTooLong() {
    String lengthExceedingBatchId = "XXXXXXXXXXXXXXXXXXXXX";
    MultipartFile mockMultipartFile = mock(MultipartFile.class);

    assertThrows(BadRequestHttpException.class, () ->
      testedInvoiceService.createMultipleInvoicesInTi(mockMultipartFile, lengthExceedingBatchId));
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
