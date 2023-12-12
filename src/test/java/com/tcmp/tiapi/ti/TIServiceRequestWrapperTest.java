package com.tcmp.tiapi.ti;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.ti.model.TIOperation;
import com.tcmp.tiapi.ti.model.TIService;
import com.tcmp.tiapi.ti.model.requests.ReplyFormat;
import com.tcmp.tiapi.ti.model.requests.ServiceRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TIServiceRequestWrapperTest {
  TIServiceRequestWrapper testedServiceRequestWrapper;

  @BeforeEach
  void beforeAll() {
    testedServiceRequestWrapper = new TIServiceRequestWrapper();
  }

  @Test
  void wrapRequest_itShouldWrapMessage() {
    String expectedInvoiceNumber = "Invoice123";

    CreateInvoiceEventMessage programmeMessage = CreateInvoiceEventMessage.builder()
      .invoiceNumber(expectedInvoiceNumber)
      .build();

    ServiceRequest<CreateInvoiceEventMessage> serviceRequest =
      testedServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.CREATE_INVOICE,
        ReplyFormat.STATUS,
        UUID.randomUUID().toString(),
        programmeMessage
      );

    String actualInvoiceNumber = serviceRequest.getBody().getInvoiceNumber();
    assertEquals(expectedInvoiceNumber, actualInvoiceNumber);
  }
}
