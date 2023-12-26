package com.tcmp.tiapi.invoice.strategy.ftireply;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.ti.dto.response.ResponseHeader;
import com.tcmp.tiapi.ti.dto.response.ResponseStatus;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingMapper;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingStatusNotifierStrategyTest {
  @Mock
  private InvoiceEventService invoiceEventService;
  @Mock private BusinessBankingService businessBankingService;
  @Mock private BusinessBankingMapper businessBankingMapper;

  @Captor
  private ArgumentCaptor<OperationalGatewayRequestPayload> payloadArgumentCaptor;

  @InjectMocks
  private InvoiceFinancingStatusNotifierStrategy invoiceFinancingStatusNotifierStrategy;
  @Test
  void handleServiceResponse_itShouldDeleteInvoiceIfFinancedSuccessfully() {
    var header =
            ResponseHeader.builder()
                    .status(ResponseStatus.SUCCESS.getValue())
                    .correlationId("123")
                    .build();
    var serviceResponse = ServiceResponse.builder().responseHeader(header).build();

    invoiceFinancingStatusNotifierStrategy.handleServiceResponse(serviceResponse);

    verify(invoiceEventService).deleteInvoiceByUuid("123");
    verifyNoInteractions(businessBankingService);
  }

  @Test
  void handleServiceResponse_itShouldNotifyIfFinancingFailed() {
    var header =
            ResponseHeader.builder()
                    .status(ResponseStatus.FAILED.getValue())
                    .correlationId("123")
                    .build();
    var serviceResponse = ServiceResponse.builder().responseHeader(header).build();

    when(invoiceEventService.findInvoiceEventInfoByUuid(anyString()))
            .thenReturn(InvoiceEventInfo.builder().build());
    when(businessBankingMapper.mapToRequestPayload(any(), any()))
            .thenReturn(OperationalGatewayRequestPayload.builder().status("FAILED").build());

    invoiceFinancingStatusNotifierStrategy.handleServiceResponse(serviceResponse);

    verify(invoiceEventService).findInvoiceEventInfoByUuid("123");
    verify(businessBankingMapper).mapToRequestPayload(any(), any());
    verify(invoiceEventService).deleteInvoiceByUuid("123");
    verify(businessBankingService).notifyEvent(any(), payloadArgumentCaptor.capture());

    assertEquals(PayloadStatus.FAILED.getValue(), payloadArgumentCaptor.getValue().status());
  }
  @Test
  void handleServiceResponse_itShouldGenerateCorrectPayload() {
    var header =
            ResponseHeader.builder()
                    .status(ResponseStatus.FAILED.getValue())
                    .correlationId("123")
                    .build();
    var serviceResponse = ServiceResponse.builder().responseHeader(header).build();

    when(invoiceEventService.findInvoiceEventInfoByUuid(header.getCorrelationId()))
            .thenReturn(InvoiceEventInfo.builder().build());
    when(businessBankingMapper.mapToRequestPayload(any(), any()))
            .thenReturn(OperationalGatewayRequestPayload.builder().status("FAILED").build());

    invoiceFinancingStatusNotifierStrategy.handleServiceResponse(serviceResponse);
    verify(businessBankingService).notifyEvent(any(), payloadArgumentCaptor.capture());
  }
}

