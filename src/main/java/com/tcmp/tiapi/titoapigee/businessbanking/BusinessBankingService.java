package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.invoice.model.InvoiceCreationEventInfo;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.*;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BusinessBankingService {
  private static final String REQUEST_PROVIDER = "FTI";

  private final HeaderSigner apiGeeHeaderSigner;
  private final BusinessBankingClient businessBankingClient;
  private final BusinessBankingMapper businessBankingMapper;

  public BusinessBankingService(
    @Qualifier("businessBankingHeaderSigner")
    HeaderSigner apiGeeHeaderSigner,
    BusinessBankingClient businessBankingClient,
    BusinessBankingMapper businessBankingMapper
  ) {
    this.apiGeeHeaderSigner = apiGeeHeaderSigner;
    this.businessBankingClient = businessBankingClient;
    this.businessBankingMapper = businessBankingMapper;
  }

  public void sendInvoiceCreationResult(ServiceResponse serviceResponse, InvoiceCreationEventInfo invoice) {
    OperationalGatewayRequestPayload requestPayload = businessBankingMapper.mapToRequestPayload(serviceResponse, invoice);

    ApiGeeBaseRequest<OperationalGatewayRequest> requestBody = ApiGeeBaseRequest.<OperationalGatewayRequest>builder()
      .data(OperationalGatewayRequest.builder()
        .referenceData(ReferenceData.builder()
          .provider(REQUEST_PROVIDER)
          .correlatedMessageId(UUID.randomUUID().toString())
          .processCode(ProcessCode.of(OperationalGatewayProcessCode.INVOICE_CREATION))
          .build())
        .payload(requestPayload)
        .build())
      .build();

    Map<String, String> requestHeaders = apiGeeHeaderSigner.buildRequestHeaders(requestBody);

    try {
      businessBankingClient.sendInvoiceCreationResult(requestHeaders, requestBody);
      log.info("Invoice creation notified successfully. Headers={} Body={}", requestHeaders, requestBody);
    } catch (FeignException e) {
      List<Integer> unrecoverableResponseCodes = List.of(
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
    }
  }
}
