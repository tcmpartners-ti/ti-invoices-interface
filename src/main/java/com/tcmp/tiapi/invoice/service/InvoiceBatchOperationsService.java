package com.tcmp.tiapi.invoice.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.model.TIOperation;
import com.tcmp.tiapi.ti.model.TIService;
import com.tcmp.tiapi.ti.model.requests.ReplyFormat;
import com.tcmp.tiapi.ti.model.requests.ServiceRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceBatchOperationsService {
  private static final int BATCH_SIZE = 100;

  private final ProducerTemplate producerTemplate;

  private final RedisTemplate<String, Object> redisTemplate;
  private final InvoiceMapper invoiceMapper;

  @Value("${ti.route.fti.out.from}")
  private String uriFtiOutgoingFrom;

  public void createMultipleInvoicesInTi(MultipartFile invoicesFile, String batchId) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      CsvToBean<InvoiceCreationRowCSV> invoiceCsvToBean =
          new CsvToBeanBuilder<InvoiceCreationRowCSV>(bufferedReader)
              .withType(InvoiceCreationRowCSV.class)
              .withSkipLines(1)
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

    TIServiceRequestWrapper wrapper = new TIServiceRequestWrapper();
    List<InvoiceEventInfo> invoiceEvents = new ArrayList<>(batchSize);
    List<ServiceRequest<CreateInvoiceEventMessage>> invoiceMessages = new ArrayList<>(batchSize);

    for (InvoiceCreationRowCSV invoiceRow : invoicesBatch) {
      String uuid = UUID.randomUUID().toString();
      invoiceEvents.add(
          InvoiceEventInfo.builder()
              .id(uuid)
              .batchId(batchId)
              .reference(invoiceRow.getInvoiceNumber())
              .sellerMnemonic(invoiceRow.getSeller())
              .build());

      invoiceMessages.add(
          wrapper.wrapRequest(
              TIService.TRADE_INNOVATION,
              TIOperation.CREATE_INVOICE,
              ReplyFormat.STATUS,
              uuid,
              invoiceMapper.mapCSVRowToFTIMessage(invoiceRow, batchId)));
    }

    saveAllInvoicesInCache(invoiceEvents);
    sendMessagesToQueue(invoiceMessages);

    log.info("Sent a batch of {} invoice(s).", batchSize);
  }

  private void saveAllInvoicesInCache(List<InvoiceEventInfo> invoiceEvents) {
    redisTemplate.executePipelined(
        (RedisCallback<Object>)
            connection -> {
              invoiceEvents.forEach(
                  invoiceEvent -> {
                    Map<String, String> hashFields = new HashMap<>();
                    hashFields.put("_class", invoiceEvent.getClass().getName());
                    hashFields.put("id", invoiceEvent.getId());
                    hashFields.put("batchId", invoiceEvent.getBatchId());
                    hashFields.put("reference", invoiceEvent.getReference());
                    hashFields.put("sellerMnemonic", invoiceEvent.getSellerMnemonic());

                    String key = "InvoiceEvent:" + invoiceEvent.getId();
                    connection
                        .hashCommands()
                        .hMSet(key.getBytes(), converStringMapToBytesMap(hashFields));
                  });

              return null;
            });
  }

  private Map<byte[], byte[]> converStringMapToBytesMap(Map<String, String> hashFields) {
    Map<byte[], byte[]> byteMap = new HashMap<>();

    for (Map.Entry<String, String> entry : hashFields.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
      byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

      byteMap.put(keyBytes, valueBytes);
    }

    return byteMap;
  }

  private void sendMessagesToQueue(
      List<ServiceRequest<CreateInvoiceEventMessage>> invoiceMessages) {
    invoiceMessages.forEach(message -> producerTemplate.asyncSendBody(uriFtiOutgoingFrom, message));
  }
}
