package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceEventService {
  private final InvoiceEventRepository invoiceEventRepository;

  public InvoiceEventInfo findInvoiceEventInfoByUuid(String invoiceUuid) {
    return invoiceEventRepository
        .findById(invoiceUuid)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    String.format("Could not find invoice with id %s.", invoiceUuid)));
  }

  public void deleteInvoiceByUuid(String invoiceUuid) {
    invoiceEventRepository.deleteById(invoiceUuid);
  }
}
