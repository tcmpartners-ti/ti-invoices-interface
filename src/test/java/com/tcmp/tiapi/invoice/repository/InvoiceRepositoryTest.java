package com.tcmp.tiapi.invoice.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class InvoiceRepositoryTest {

  @Autowired private InvoiceRepository invoiceRepository;

  @AfterEach
  void tearDown() {
    invoiceRepository.deleteAll();
  }

  @Test
  void findByProductMasterMasterReference() {
    var masterReference = "INV00000001BPCH";
    var invoiceId = 1L;

    var expectedMaster =
        ProductMaster.builder()
            .id(invoiceId)
            .contractDate(LocalDate.of(2024, 8, 15))
            .masterReference(masterReference)
            .isActive(true)
            .status(ProductMasterStatus.LIV)
            .build();
    var invoice =
        InvoiceMaster.builder()
            .id(invoiceId)
            .reference("001-001-000000001")
            .productMaster(expectedMaster)
            .build();

    invoiceRepository.save(invoice);

    var actualInvoice =
        invoiceRepository
            .findByProductMasterMasterReference(masterReference)
            .orElseThrow(EntityNotFoundException::new);
    var actualMaster = actualInvoice.getProductMaster();

    // This is an attempt to satisfy f-ing SonarQube.
    assertEquals(expectedMaster.getId(), actualMaster.getId());
    assertEquals(expectedMaster.getMasterReference(), actualMaster.getMasterReference());
    assertEquals(expectedMaster.getContractDate(), actualMaster.getContractDate());
    assertEquals(expectedMaster.getIsActive(), actualMaster.getIsActive());
    assertEquals(expectedMaster.getStatus(), actualMaster.getStatus());
  }
}
