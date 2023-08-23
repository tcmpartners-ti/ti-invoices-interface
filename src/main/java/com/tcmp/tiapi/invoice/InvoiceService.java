package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.Credentials;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.model.requests.RequestHeader;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private final ProducerTemplate producerTemplate;

  private final InvoiceConfiguration invoiceConfiguration;
  private final InvoiceRepository invoiceRepository;

  public InvoiceMaster getInvoiceByReference(String reference) {
    return invoiceRepository.findByReference(reference)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find an invoice with reference %s.", reference)));
  }

  public String sendInvoiceAndGetCorrelationId(CreateInvoiceEventMessage createInvoiceEventMessage) {
    String invoiceCorrelationId = UUID.randomUUID().toString();

    RequestHeader requestHeader = RequestHeader.builder()
      .service(TIService.TRADE_INNOVATION.getValue())
      .operation(TIOperation.CREATE_INVOICE.getValue())
      .replyFormat(ReplyFormat.STATUS.getValue())
      .correlationId(invoiceCorrelationId)
      .credentials(Credentials.builder()
        .name("FTI_INTERFACE")
        .build())
      .build();

    ServiceRequest<CreateInvoiceEventMessage> createInvoiceEventMessageServiceRequest =
      ServiceRequest.<CreateInvoiceEventMessage>builder()
        .header(requestHeader)
        .body(createInvoiceEventMessage)
        .build();

    log.info("[CREATE INVOICE] {}", createInvoiceEventMessage);

    producerTemplate.sendBodyAndHeaders(
      invoiceConfiguration.getUriCreateFrom(),
      createInvoiceEventMessageServiceRequest,
      Map.of(
        "JMSCorrelationID", invoiceCorrelationId
      )
    );

    return invoiceCorrelationId;
  }

  // Improve this method
  public void createMultipleInvoices(MultipartFile invoicesFile) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try {
      InputStreamReader inputStreamReader = new InputStreamReader(invoicesFile.getInputStream());

      try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
        producerTemplate.sendBody(invoiceConfiguration.getUriCreateFrom(), bufferedReader);
      }
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not read the uploaded file");
    }
  }
}
