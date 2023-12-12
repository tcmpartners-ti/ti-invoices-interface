package com.tcmp.tiapi.titoapigee.businessbanking;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.ti.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessBankingServiceTest {
  @Mock private HeaderSigner businessBankingHeaderSigner;
  @Mock private BusinessBankingClient businessBankingClient;
  @Mock private BusinessBankingMapper businessBankingMapper;

  @Captor private ArgumentCaptor<Map<String, String>> headersArgumentCaptor;
  @Captor private ArgumentCaptor<ApiGeeBaseRequest<OperationalGatewayRequest<?>>> bodyArgumentCaptor;

  private BusinessBankingService testedService;

  @BeforeEach
  void setUp() {
    testedService = new BusinessBankingService(
      businessBankingHeaderSigner,
      businessBankingClient,
      businessBankingMapper
    );
  }

  @Test
  void sendInvoiceEventResult_itShouldNotifyInvoiceEvent() {
    var processCode = OperationalGatewayProcessCode.INVOICE_SETTLEMENT;
    var serviceResponse = ServiceResponse.builder().build();
    var invoiceEventInfo = InvoiceEventInfo.builder().build();

    when(businessBankingMapper.mapToRequestPayload(any(), any()))
      .thenReturn(OperationalGatewayRequestPayload.builder().build());

    testedService.notifyInvoiceEventResult(processCode, serviceResponse, invoiceEventInfo);

    verify(businessBankingClient).notifyEvent(headersArgumentCaptor.capture(), bodyArgumentCaptor.capture());

    assertNotNull(headersArgumentCaptor.getValue());
    assertNotNull(bodyArgumentCaptor.getValue().data().referenceData());
    assertNotNull(bodyArgumentCaptor.getValue().data().payload());
  }

  @Test
  void sendInvoiceEventResult_itShouldThrowUnrecoverableExceptionWhenResponseCodeIsUnrecoverable() {
    var processCode = OperationalGatewayProcessCode.INVOICE_SETTLEMENT;
    var serviceResponse = ServiceResponse.builder().build();
    var invoiceEventInfo = InvoiceEventInfo.builder().build();

    Request request = Request.create(Request.HttpMethod.POST, "", Map.of(), null, new RequestTemplate());
    when(businessBankingClient.notifyEvent(anyMap(), any()))
      .thenThrow(new FeignException.GatewayTimeout("", request, null, null));

    assertThrows(UnrecoverableApiGeeRequestException.class,
      () -> testedService.notifyInvoiceEventResult(processCode, serviceResponse, invoiceEventInfo));
  }

  @Test
  void sendInvoiceEventResult_itShouldThrowRecoverableExceptionWhenResponseCodeIsRecoverable() {
    var processCode = OperationalGatewayProcessCode.INVOICE_SETTLEMENT;
    var serviceResponse = ServiceResponse.builder().build();
    var invoiceEventInfo = InvoiceEventInfo.builder().build();

    Request request = Request.create(Request.HttpMethod.POST, "", Map.of(), null, new RequestTemplate());
    when(businessBankingClient.notifyEvent(anyMap(), any()))
      .thenThrow(new FeignException.TooManyRequests("", request, null, null));

    assertThrows(RecoverableApiGeeRequestException.class,
      () -> testedService.notifyInvoiceEventResult(processCode, serviceResponse, invoiceEventInfo));
  }
}
