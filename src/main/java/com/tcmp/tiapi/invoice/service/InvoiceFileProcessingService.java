package com.tcmp.tiapi.invoice.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.tcmp.tiapi.invoice.InvoiceConfiguration;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.validation.InvoiceRowValidator;
import com.tcmp.tiapi.shared.exception.CsvValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceFileProcessingService {
  private final ProducerTemplate producerTemplate;

  private final InvoiceConfiguration invoiceConfiguration;
  private final InvoiceMapper invoiceMapper;
  private final InvoiceRowValidator invoiceRowValidator;

  private final Validator validator;

  public void processFile(String batchId, BufferedReader invoicesFileBufferedReader) {
    CsvToBean<InvoiceCreationRowCSV> invoiceCsvToBean = buildBeansFromBufferedReader(invoicesFileBufferedReader);

    var rowIndex = 1;
    List<FieldError> fileViolations = new ArrayList<>();
    for (InvoiceCreationRowCSV invoice : invoiceCsvToBean) {
      List<FieldError> rowViolations = invoiceRowValidator.validate(rowIndex++, invoice, validator);
      fileViolations.addAll(rowViolations);

      if (rowViolations.isEmpty()) mapInvoiceRowAndSendToQueue(batchId, invoice);
    }

    if (!fileViolations.isEmpty()) {
      throw new CsvValidationException("Could not validate the provided file.", fileViolations);
    }
  }

  private CsvToBean<InvoiceCreationRowCSV> buildBeansFromBufferedReader(BufferedReader bufferedReader) {
    return new CsvToBeanBuilder<InvoiceCreationRowCSV>(bufferedReader)
      .withSkipLines(1)
      .withType(InvoiceCreationRowCSV.class)
      .withSeparator(',')
      .withIgnoreEmptyLine(true)
      .build();
  }

  private void mapInvoiceRowAndSendToQueue(String batchId, InvoiceCreationRowCSV invoiceRow) {
    CreateInvoiceEventMessage invoiceMessage = invoiceMapper.mapCSVRowToFTIMessage(invoiceRow, batchId);

    producerTemplate.sendBody(
      invoiceConfiguration.getUriCreateFrom(),
      invoiceMessage
    );
  }
}
