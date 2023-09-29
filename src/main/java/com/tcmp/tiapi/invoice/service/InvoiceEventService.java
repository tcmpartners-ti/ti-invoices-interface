package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceCreationEventInfo;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceCreationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceEventService {
  private final InvoiceCreationEventRepository invoiceCreationEventRepository;

  public InvoiceCreationEventInfo findInvoiceByUuid(String invoiceUuid) {
    return invoiceCreationEventRepository.findById(invoiceUuid)
      .orElseThrow(() -> new RuntimeException(String.format("Could not find invoice with id %s.", invoiceUuid)));
  }

  public void saveInvoiceInfoFromCreationMessage(String invoiceUuid, CreateInvoiceEventMessage invoiceEventMessage) {
    InvoiceCreationEventInfo invoiceInfo = InvoiceCreationEventInfo.builder()
      .id(invoiceUuid)
      .batchId(invoiceEventMessage.getBatchId())
      .reference(invoiceEventMessage.getInvoiceNumber())
      .buyerMnemonic(invoiceEventMessage.getBuyer())
      .build();

    invoiceCreationEventRepository.save(invoiceInfo);
  }

  public void deleteInvoiceByUuid(String invoiceUuid) {
    invoiceCreationEventRepository.deleteById(invoiceUuid);
  }
}
