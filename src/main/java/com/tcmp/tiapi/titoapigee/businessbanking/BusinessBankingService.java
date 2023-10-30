package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.ProcessCode;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.ReferenceData;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessBankingService {
  private static final String REQUEST_PROVIDER = "FTI";

  private final HeaderSigner businessBankingHeaderSigner;
  private final BusinessBankingClient businessBankingClient;
  private final BusinessBankingMapper businessBankingMapper;

  public void sendInvoiceEventResult(
    OperationalGatewayProcessCode processCode,
    ServiceResponse serviceResponse,
    InvoiceEventInfo invoice
  ) {
    OperationalGatewayRequestPayload payload = businessBankingMapper.mapToRequestPayload(serviceResponse, invoice);

    ApiGeeBaseRequest<OperationalGatewayRequest> body = ApiGeeBaseRequest.<OperationalGatewayRequest>builder()
      .data(OperationalGatewayRequest.builder()
        .referenceData(ReferenceData.builder()
          .provider(REQUEST_PROVIDER)
          .correlatedMessageId(UUID.randomUUID().toString())
          .processCode(ProcessCode.of(processCode))
          .build())
        .payload(payload)
        .build())
      .build();

    Map<String, String> headers = businessBankingHeaderSigner.buildRequestHeaders(body);

    try {
      businessBankingClient.sendInvoiceEventResult(headers, body);
      log.info("Invoice creation notified successfully");
    } catch (FeignException e) {
      List<Integer> unrecoverableResponseCodes = List.of(
        HttpStatus.GATEWAY_TIMEOUT.value(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.UNAUTHORIZED.value(),
        HttpStatus.FORBIDDEN.value(),
        HttpStatus.NOT_FOUND.value()
      );

      boolean isUnrecoverableRequest = unrecoverableResponseCodes.contains(e.status());
      if (isUnrecoverableRequest) {
        log.error("Client error. {}.", e.getMessage());
        throw new UnrecoverableApiGeeRequestException(e.getMessage());
      }

      log.error("Server error. {}.", e.getMessage());
      throw new RecoverableApiGeeRequestException(e.getMessage());
    } finally {
      log.info("Headers={} Body={}", headers, body);
    }
  }
}
