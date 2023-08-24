package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
  @Mock
  private ProducerTemplate producerTemplate;
  @Mock
  private InvoiceConfiguration invoiceConfiguration;
  @Mock
  private InvoiceRepository invoiceRepository;

  private InvoiceService testedInvoiceService;

  @BeforeEach
  void setUp() {
    testedInvoiceService = new InvoiceService(
      producerTemplate,
      invoiceConfiguration,
      invoiceRepository
    );
  }

  @Test
  void itShouldGetInvoiceByReference() {
    String invoiceReference = "INV123";

    when(invoiceRepository.findByReference(invoiceReference))
      .thenReturn(Optional.of(InvoiceMaster.builder()
        .id(1L)
        .reference(invoiceReference)
        .build()));

    InvoiceMaster invoiceMaster = testedInvoiceService.getInvoiceByReference(invoiceReference);

    verify(invoiceRepository).findByReference(invoiceReference);
    assertNotNull(invoiceMaster);
  }

  @Test
  void itShouldThrowExceptionWhenInvoiceNotFoundByReference() {
    String invoiceReference = "INV123";

    when(invoiceRepository.findByReference(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedInvoiceService.getInvoiceByReference(invoiceReference),
      String.format("Could not find an invoice with reference %s.", invoiceReference));
  }

  @Test
  void itShouldInvokeCamelRouteWhenCreatingInvoice() {
    CreateInvoiceEventMessage createInvoiceEventMessage = CreateInvoiceEventMessage.builder().build();

    ArgumentCaptor<String> routeCaptor =
      ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<CreateInvoiceEventMessage> messageCaptor =
      ArgumentCaptor.forClass(CreateInvoiceEventMessage.class);
    ArgumentCaptor<Map<String, Object>> headersCaptor =
      ArgumentCaptor.forClass(Map.class);

    String expectedRoute = "direct:mockCreate";

    when(invoiceConfiguration.getUriCreateFrom())
      .thenReturn(expectedRoute);

    testedInvoiceService.sendInvoiceAndGetCorrelationId(createInvoiceEventMessage);

    verify(producerTemplate).sendBodyAndHeaders(
      routeCaptor.capture(),
      messageCaptor.capture(),
      headersCaptor.capture()
    );
    assertThat(routeCaptor.getValue()).isEqualTo(expectedRoute);
    assertThat(messageCaptor.getValue()).isEqualTo(createInvoiceEventMessage);
    assertThat(headersCaptor.getValue().get("JMSCorrelationID")).isNotNull();
  }


  @Test
  void itShouldSendToCamelRouteIfFileIsCorrect() throws IOException {
    String fileContent = "A,B,C";
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    MultipartFile multipartFile = new MockMultipartFile("invoices.csv", inputStream);
    String expectedRoute = "direct:createBulkInvoices";

    ArgumentCaptor<String> routeCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BufferedReader> bufferedReaderCaptor = ArgumentCaptor.forClass(BufferedReader.class);
    when(invoiceConfiguration.getUriBulkCreateFrom())
      .thenReturn(expectedRoute);

    testedInvoiceService.createMultipleInvoices(multipartFile);

    verify(producerTemplate).sendBody(routeCaptor.capture(), bufferedReaderCaptor.capture());
    assertThat(routeCaptor.getValue()).isEqualTo(expectedRoute);
  }

  @Test
  void itShouldThrowExceptionWhenFileIsEmpty() {
    // Prepare test data
    MultipartFile multipartFile = new MockMultipartFile("invoices.csv", new byte[0]);

    // Call the method and assert exception
    assertThrows(InvalidFileHttpException.class,
      () -> testedInvoiceService.createMultipleInvoices(multipartFile));
  }

  @Test
  void itShouldThrowExceptionWhenInvalidFile() throws IOException {
    MultipartFile problematicFile = mock(MultipartFile.class);

    when(problematicFile.getInputStream())
      .thenThrow(new IOException("Simulated read error"));

    assertThrows(InvalidFileHttpException.class,
      () -> testedInvoiceService.createMultipleInvoices(problematicFile));
  }
}
