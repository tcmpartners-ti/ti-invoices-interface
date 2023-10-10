package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceCreationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceEventService {
  private final InvoiceCreationEventRepository invoiceCreationEventRepository;

  public InvoiceEventInfo findInvoiceEventInfoByUuid(String invoiceUuid) {
    return invoiceCreationEventRepository.findById(invoiceUuid)
      .orElseThrow(() -> new RuntimeException(String.format("Could not find invoice with id %s.", invoiceUuid)));
  }

  public void saveInvoiceInfoFromCreationMessage(String invoiceUuid, CreateInvoiceEventMessage invoiceEventMessage) {
    InvoiceEventInfo invoiceInfo = InvoiceEventInfo.builder()
      .id(invoiceUuid)
      .batchId(invoiceEventMessage.getBatchId())
      .reference(invoiceEventMessage.getInvoiceNumber())
      .sellerMnemonic(invoiceEventMessage.getSeller())
      .build();

    invoiceCreationEventRepository.save(invoiceInfo);
  }

  public void saveInvoiceEventInfoFromFinanceMessage(String invoiceUuid, FinanceBuyerCentricInvoiceEventMessage invoiceEventMessage) {
    String invoiceNumber = invoiceEventMessage.getInvoiceNumbersContainer().getInvoiceNumbers().get(0).getInvoiceNumber();

    InvoiceEventInfo invoiceInfo = InvoiceEventInfo.builder()
      .id(invoiceUuid)
      .reference(invoiceNumber)
      .sellerMnemonic(invoiceEventMessage.getSeller())
      .build();

    invoiceCreationEventRepository.save(invoiceInfo);
  }

  public void deleteInvoiceByUuid(String invoiceUuid) {
    invoiceCreationEventRepository.deleteById(invoiceUuid);
  }
}
