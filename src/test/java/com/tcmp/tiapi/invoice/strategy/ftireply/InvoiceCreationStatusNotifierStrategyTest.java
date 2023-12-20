package com.tcmp.tiapi.invoice.strategy.ftireply;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.ti.dto.response.ResponseHeader;
import com.tcmp.tiapi.ti.dto.response.ResponseStatus;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingMapper;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceCreationStatusNotifierStrategyTest {
  @Mock private InvoiceEventService invoiceEventService;
  @Mock private BusinessBankingService businessBankingService;
  @Mock private BusinessBankingMapper businessBankingMapper;

  private InvoiceCreationStatusNotifierStrategy invoiceCreationStatusNotifierStrategy;

  @BeforeEach
  void setUp() {
    invoiceCreationStatusNotifierStrategy =
        new InvoiceCreationStatusNotifierStrategy(
            invoiceEventService, businessBankingService, businessBankingMapper);
  }

  @Test
  void handleServiceResponse_itShouldDeleteInvoiceIfCreatedSuccessfully() {
    var serviceResponse =
        ServiceResponse.builder()
            .responseHeader(
                ResponseHeader.builder()
                    .status(ResponseStatus.SUCCESS.getValue())
                    .correlationId("123")
                    .build())
            .build();

    invoiceCreationStatusNotifierStrategy.handleServiceResponse(serviceResponse);

    verify(invoiceEventService).deleteInvoiceByUuid("123");
  }

  @Test
  void handleServiceResponse_itShouldNotifyIfCreationFailed() {
    var serviceResponse =
        ServiceResponse.builder()
            .responseHeader(
                ResponseHeader.builder()
                    .status(ResponseStatus.FAILED.getValue())
                    .correlationId("123")
                    .build())
            .build();

    when(invoiceEventService.findInvoiceEventInfoByUuid(anyString()))
        .thenReturn(InvoiceEventInfo.builder().build());
    when(businessBankingMapper.mapToRequestPayload(any(), any()))
        .thenReturn(OperationalGatewayRequestPayload.builder().build());

    invoiceCreationStatusNotifierStrategy.handleServiceResponse(serviceResponse);

    verify(invoiceEventService).findInvoiceEventInfoByUuid("123");
    verify(businessBankingMapper).mapToRequestPayload(any(), any());

    verify(invoiceEventService).deleteInvoiceByUuid("123");
  }
}