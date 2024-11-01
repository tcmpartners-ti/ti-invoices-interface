package com.tcmp.tiapi.invoice.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.repository.InvoiceCacheRepository;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.ReplyFormat;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceBatchOperationsService {
  private static final int BATCH_SIZE = 100;

  private final Clock clock;
  private final ProducerTemplate producerTemplate;

  private final InvoiceCacheRepository invoiceCacheRepository;
  private final BulkCreateInvoicesFileInfoRepository bulkCreateInvoicesFileInfoRepository;
  private final InvoiceMapper invoiceMapper;
  private final TIServiceRequestWrapper wrapper;
  private final UUIDGenerator uuidGenerator;

  @Value("${ti.route.fti.out.from}")
  private String uriFtiOutgoingFrom;

  public void createInvoicesInTiWithBusinessBankingChannel(
      MultipartFile invoicesFile, String batchId) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      CsvToBean<InvoiceCreationRowCSV> invoiceCsvToBean =
          new CsvToBeanBuilder<InvoiceCreationRowCSV>(bufferedReader)
              .withType(InvoiceCreationRowCSV.class)
              .withIgnoreEmptyLine(true)
              .build();

      processFileAsBatches(batchId, invoiceCsvToBean);
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not process file");
    }
  }

  private void processFileAsBatches(
      String batchId, CsvToBean<InvoiceCreationRowCSV> invoiceCsvToBean) {
    List<InvoiceCreationRowCSV> invoicesBatch = new ArrayList<>();
    // Skip header
    int currentLine = 1;

    for (InvoiceCreationRowCSV invoiceRow : invoiceCsvToBean) {
      invoicesBatch.add(invoiceRow);

      if (currentLine % BATCH_SIZE == 0) {
        processBatch(batchId, invoicesBatch);
        invoicesBatch.clear();
      }

      currentLine++;
    }

    boolean batchHasRemainingRows = !invoicesBatch.isEmpty();
    if (batchHasRemainingRows) {
      processBatch(batchId, invoicesBatch);
    }
  }

  private void processBatch(String batchId, List<InvoiceCreationRowCSV> invoicesBatch) {
    int batchSize = invoicesBatch.size();

    List<InvoiceEventInfo> invoiceEvents = new ArrayList<>(batchSize);
    List<ServiceRequest<CreateInvoiceEventMessage>> invoiceMessages = new ArrayList<>(batchSize);

    for (InvoiceCreationRowCSV invoiceRow : invoicesBatch) {
      String uuid = uuidGenerator.getNewId();

      InvoiceEventInfo invoiceInfo =
          InvoiceEventInfo.builder()
              .id(uuid)
              .batchId(batchId)
              .reference(invoiceRow.getInvoiceNumber())
              .sellerMnemonic(invoiceRow.getSeller())
              .build();
      invoiceEvents.add(invoiceInfo);

      ServiceRequest<CreateInvoiceEventMessage> tiMessage =
          wrapper.wrapRequest(
              TIService.TRADE_INNOVATION,
              TIOperation.CREATE_INVOICE,
              ReplyFormat.STATUS,
              uuid,
              invoiceMapper.mapCSVRowToFTIMessage(invoiceRow, batchId));
      invoiceMessages.add(tiMessage);
    }

    invoiceCacheRepository.saveAll(invoiceEvents);
    sendMessagesToQueue(invoiceMessages);

    log.info("Sent a batch of {} invoice(s). Channel: business banking.", batchSize);
  }

  public void createInvoicesInTIWithSftpChannel(MultipartFile invoicesFile, String batchId) {

    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    String fileUuid = uuidGenerator.getNewId();

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      CsvToBean<InvoiceCreationRowCSV> invoiceCsvToBean =
          new CsvToBeanBuilder<InvoiceCreationRowCSV>(bufferedReader)
              .withType(InvoiceCreationRowCSV.class)
              .withIgnoreEmptyLine(true)
              .build();

      int totalProcessedInvoices = processSftpChannelBatch(fileUuid, batchId, invoiceCsvToBean);

      BulkCreateInvoicesFileInfo fileInfo =
          BulkCreateInvoicesFileInfo.builder()
              .id(fileUuid)
              .totalInvoices(totalProcessedInvoices)
              .originalFilename(invoicesFile.getOriginalFilename())
              .receivedAt(LocalDateTime.now(clock))
              .build();
      bulkCreateInvoicesFileInfoRepository.save(fileInfo);
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not process file");
    }
  }

  /**
   * @see com.tcmp.tiapi.invoice.strategy.ftireply.InvoiceCreationStatusNotifierStrategy
   * @param batchCorrelationId Batch correlation id to use when response is received
   * @param batchId Batch identifier to store in TI
   * @param invoices List of invoices to process
   * @return Total invoices processed
   */
  private int processSftpChannelBatch(
      String batchCorrelationId, String batchId, CsvToBean<InvoiceCreationRowCSV> invoices) {
    int totalInvoices = 0;

    for (InvoiceCreationRowCSV invoiceRow : invoices) {
      String correlationId = batchCorrelationId + ":" + invoiceRow.getIndex();

      ServiceRequest<CreateInvoiceEventMessage> message =
          wrapper.wrapRequest(
              TIService.TRADE_INNOVATION,
              TIOperation.CREATE_INVOICE,
              ReplyFormat.STATUS,
              correlationId,
              invoiceMapper.mapCSVRowToFTIMessage(invoiceRow, batchId));

      producerTemplate.asyncSendBody(uriFtiOutgoingFrom, message);
      totalInvoices++;
    }

    log.info("Sent {} invoice(s). Channel: SFTP.", totalInvoices);

    return totalInvoices;
  }

  private void sendMessagesToQueue(
      List<ServiceRequest<CreateInvoiceEventMessage>> invoiceMessages) {
    invoiceMessages.forEach(message -> producerTemplate.asyncSendBody(uriFtiOutgoingFrom, message));
  }
}
