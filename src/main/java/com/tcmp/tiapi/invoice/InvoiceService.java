package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.shared.exception.BadRequestHttpException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private static final int MAX_BATCH_ID_LENGTH = 20;

  private final ProducerTemplate producerTemplate;

  private final InvoiceConfiguration invoiceConfiguration;
  private final InvoiceRepository invoiceRepository;

  public InvoiceMaster getInvoiceByReference(String reference) {
    return invoiceRepository.findByReference(reference)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find an invoice with reference %s.", reference)));
  }

  public void sendInvoiceAndGetCorrelationId(CreateInvoiceEventMessage createInvoiceEventMessage) {
    log.info("[Invoice: Create] {}", createInvoiceEventMessage);

    producerTemplate.sendBodyAndHeaders(
      invoiceConfiguration.getUriCreateFrom(),
      createInvoiceEventMessage,
      Map.ofEntries(
        Map.entry("JMSCorrelationID", createInvoiceEventMessage.getInvoiceNumber())
      )
    );
  }

  public void createMultipleInvoices(MultipartFile invoicesFile, String batchId) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    if (batchId.length() > MAX_BATCH_ID_LENGTH) {
      throw new BadRequestHttpException("Batch id must be up to 20 characters.");
    }

    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      log.info("[Invoice: bulk create] Sending invoices to TI.");

      producerTemplate.sendBodyAndHeaders(
        invoiceConfiguration.getUriBulkCreateFrom(),
        bufferedReader,
        Map.ofEntries(
          Map.entry("batchId", batchId)
        )
      );
    } catch (IOException e) {
      log.error("[Invoice: bulk create] Invalid file uploaded");
      throw new InvalidFileHttpException("Could not read the uploaded file");
    }
  }
}
