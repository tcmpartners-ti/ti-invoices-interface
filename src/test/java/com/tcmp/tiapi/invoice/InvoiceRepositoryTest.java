package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class InvoiceRepositoryTest {

  @Autowired
  private InvoiceRepository testedInvoiceRepository;

  @AfterEach
  void tearDown() {
    testedInvoiceRepository.deleteAll();
  }

  @Test
  void itShouldFindInvoiceByReference() {
    String invoiceReference = "INVOICE123";

    testedInvoiceRepository.save(InvoiceMaster.builder()
      .id(1L)
      .reference(invoiceReference)
      .build());

    Optional<InvoiceMaster> actualInvoice = testedInvoiceRepository.findByReference(invoiceReference);

    assertTrue(actualInvoice.isPresent());
  }

  @Test
  void itShouldFindByBuyerId() {
    Long buyerId = 1L;

    testedInvoiceRepository.save(InvoiceMaster.builder()
      .id(1L)
      .reference("INVOICE1")
      .buyerId(buyerId)
      .build());

    testedInvoiceRepository.save(InvoiceMaster.builder()
      .id(2L)
      .reference("INVOICE2")
      .buyerId(buyerId)
      .build());

    Page<InvoiceMaster> invoiceMasterPage = testedInvoiceRepository.findByBuyerIdIn(List.of(buyerId),
      PageRequest.of(0, 10));

    assertNotNull(invoiceMasterPage);
    assertThat(invoiceMasterPage).hasSize(2);
  }
}
