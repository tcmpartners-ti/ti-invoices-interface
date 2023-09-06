package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.model.Program;
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
  void itShouldMapEntityToDTO() {
    InvoiceMaster source = InvoiceMaster.builder()
      .id(1L)
      .reference("Invoice123")
      .build();
    CounterParty buyer = CounterParty.builder()
      .id(1L)
      .mnemonic("1722466421001")
      .build();
    CounterParty seller = CounterParty.builder()
      .id(2L)
      .mnemonic("1722466422001")
      .build();
    Program program = Program.builder()
      .pk(1L)
      .id("IDEAL01")
      .build();

    InvoiceDTO invoiceDto = testedInvoiceMapper.mapEntityToDTO(source, buyer, seller, program);

    assertEquals(source.getId(), invoiceDto.getId());
    assertEquals(buyer.getId(), invoiceDto.getBuyer().getId());
    assertEquals(seller.getId(), invoiceDto.getSeller().getId());
    assertEquals(program.getId(), invoiceDto.getProgramme().getId());
  }

  @Test
  void itShouldReturnNullIfEverySourceIsNull() {
    InvoiceDTO invoiceDTO = testedInvoiceMapper.mapEntityToDTO(null, null, null, null);

    assertNull(invoiceDTO);
  }

  @Test
  void itShouldMapDTOToFTIMessage() {
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
  void itShouldMapCSVRowToFTIMessage() {
    InvoiceCreationRowCSV invoiceRow = InvoiceCreationRowCSV.builder()
      .branch("BPEC")
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
