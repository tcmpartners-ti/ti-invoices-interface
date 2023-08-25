package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
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

    log.info("[Create Invoice] {}", createInvoiceEventMessage);

    producerTemplate.sendBodyAndHeaders(
      invoiceConfiguration.getUriCreateFrom(),
      createInvoiceEventMessage,
      Map.ofEntries(
        Map.entry("JMSCorrelationID", invoiceCorrelationId)
      )
    );

    return invoiceCorrelationId;
  }

  public void createMultipleInvoices(MultipartFile invoicesFile) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      log.info("[Bulk create invoices] Sending invoices to TI.");

      producerTemplate.sendBody(
        invoiceConfiguration.getUriBulkCreateFrom(),
        bufferedReader
      );
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not read the uploaded file");
    }
  }
}
