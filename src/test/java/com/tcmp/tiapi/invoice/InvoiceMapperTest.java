package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class InvoiceMapperTest {
  @Autowired InvoiceMapper testedInvoiceMapper;

  @Test
  void mapEntityToDTO_itShouldReturnNullIfEverySourceIsNull() {
    InvoiceDTO invoiceDTO = testedInvoiceMapper.mapEntityToDTO(null);

    assertNull(invoiceDTO);
  }

  @Test
  void mapDTOToFTIMessage_itShouldMapDTOToFTIMessage() {
    InvoiceCreationDTO invoiceCreationDTO = InvoiceCreationDTO.builder()
      .invoiceNumber("Invoice123")
      .buyer("Supermaxi")
      .seller("CocaCola")
      .programme("SUP123")
      .build();

    CreateInvoiceEventMessage createInvoiceMessage = testedInvoiceMapper.mapDTOToFTIMessage(invoiceCreationDTO);

    assertEquals(invoiceCreationDTO.getInvoiceNumber(), createInvoiceMessage.getInvoiceNumber());
    assertEquals(invoiceCreationDTO.getBuyer(), createInvoiceMessage.getBuyer());
    assertEquals(invoiceCreationDTO.getSeller(), createInvoiceMessage.getSeller());
    assertEquals(invoiceCreationDTO.getProgramme(), createInvoiceMessage.getProgramme());
  }

  @Test
  void mapCSVRowToFTIMessage_itShouldMapCSVRowToFTIMessage() {
    InvoiceCreationRowCSV invoiceRow = InvoiceCreationRowCSV.builder()
      .behalfOfBranch("BPEC")
      .buyerId("Supermaxi")
      .sellerId("CocaCola")
      .programmeId("SUP123")
      .faceValueAmount(BigDecimal.TEN)
      .outstandingAmount(BigDecimal.TEN)
      .build();

    CreateInvoiceEventMessage createInvoiceMessage = testedInvoiceMapper.mapCSVRowToFTIMessage(invoiceRow, null);

    assertEquals(invoiceRow.getInvoiceNumber(), createInvoiceMessage.getInvoiceNumber());
    assertEquals(invoiceRow.getBuyerId(), createInvoiceMessage.getBuyer());
    assertEquals(invoiceRow.getSellerId(), createInvoiceMessage.getSeller());
    assertEquals(invoiceRow.getProgrammeId(), createInvoiceMessage.getProgramme());
  }
}
