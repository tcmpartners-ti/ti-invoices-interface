package com.tcmp.tiapi.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class InvoiceMapperTest {
  private final InvoiceMapper testedInvoiceMapper = Mappers.getMapper(InvoiceMapper.class);

  @Test
  void mapDTOToFTIMessage_itShouldMapDTOToFTIMessage() {
    InvoiceCreationDTO invoiceCreationDTO =
        InvoiceCreationDTO.builder()
            .invoiceNumber("Invoice123")
            .buyer("Supermaxi")
            .seller("CocaCola")
            .programme("SUP123")
            .issueDate("01-01-2023")
            .settlementDate("02-02-2023")
            .build();

    CreateInvoiceEventMessage createInvoiceMessage =
        testedInvoiceMapper.mapDTOToFTIMessage(invoiceCreationDTO);

    assertEquals(invoiceCreationDTO.getInvoiceNumber(), createInvoiceMessage.getInvoiceNumber());
    assertEquals(invoiceCreationDTO.getBuyer(), createInvoiceMessage.getBuyer());
    assertEquals(invoiceCreationDTO.getSeller(), createInvoiceMessage.getSeller());
    assertEquals(invoiceCreationDTO.getProgramme(), createInvoiceMessage.getProgramme());
  }

  @Test
  void mapCSVRowToFTIMessage_itShouldMapCSVRowToFTIMessage() {
    InvoiceCreationRowCSV invoiceRow =
        InvoiceCreationRowCSV.builder()
            .behalfOfBranch("BPEC")
            .buyer("Supermaxi")
            .seller("CocaCola")
            .programme("SUP123")
            .build();

    CreateInvoiceEventMessage createInvoiceMessage =
        testedInvoiceMapper.mapCSVRowToFTIMessage(invoiceRow, null);

    assertEquals(invoiceRow.getInvoiceNumber(), createInvoiceMessage.getInvoiceNumber());
    assertEquals(invoiceRow.getBuyer(), createInvoiceMessage.getBuyer());
    assertEquals(invoiceRow.getSeller(), createInvoiceMessage.getSeller());
    assertEquals(invoiceRow.getProgramme(), createInvoiceMessage.getProgramme());
  }
}
