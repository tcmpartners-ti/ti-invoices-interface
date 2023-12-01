package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceCreationEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceEventService {
  private final InvoiceRepository invoiceRepository;
  private final InvoiceCreationEventRepository invoiceCreationEventRepository;

  public InvoiceEventInfo findInvoiceEventInfoByUuid(String invoiceUuid) {
    return invoiceCreationEventRepository.findById(invoiceUuid)
      .orElseThrow(() -> new EntityNotFoundException(String.format("Could not find invoice with id %s.", invoiceUuid)));
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

  // Hotfix: find a better and cleaner way of doing this.
  public void saveInvoiceEventInfoFromFinanceMessage(String invoiceUuid, FinanceBuyerCentricInvoiceEventMessage invoiceEventMessage) {
    String invoiceNumber = invoiceEventMessage.getInvoiceNumbersContainer().getInvoiceNumbers().get(0).getInvoiceNumber();
    InvoiceMaster invoice = findInvoiceFromFinanceMessage(invoiceEventMessage);

    InvoiceEventInfo invoiceInfo = InvoiceEventInfo.builder()
      .id(invoiceUuid)
      .batchId(invoice.getBatchId().trim())
      .reference(invoiceNumber)
      .sellerMnemonic(invoiceEventMessage.getSeller())
      .build();

    invoiceCreationEventRepository.save(invoiceInfo);
  }

  // Hotfix: find a better and cleaner way of doing this.
  private InvoiceMaster findInvoiceFromFinanceMessage(FinanceBuyerCentricInvoiceEventMessage invoiceEventMessage) {
    return invoiceRepository.findByProgramIdAndSellerMnemonicAndReference(
      invoiceEventMessage.getProgramme(),
      invoiceEventMessage.getSeller(),
      invoiceEventMessage.getTheirReference()
    ).orElseThrow(
      () -> new EntityNotFoundException("Could not find invoice for given programme / buyer / seller relationship."));
  }

  public void deleteInvoiceByUuid(String invoiceUuid) {
    invoiceCreationEventRepository.deleteById(invoiceUuid);
  }
}
