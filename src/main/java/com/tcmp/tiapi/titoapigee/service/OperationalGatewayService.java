package com.tcmp.tiapi.titoapigee.service;

import com.tcmp.tiapi.invoice.dto.request.InvoiceNotificationPayload;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.client.ApiGeeHeaderSigner;
import com.tcmp.tiapi.titoapigee.client.OperationalGatewayClient;
import com.tcmp.tiapi.titoapigee.dto.request.*;
import com.tcmp.tiapi.titoapigee.exception.RecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.mapper.OperationalGatewayMapper;
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
public class OperationalGatewayService {
  private final OperationalGatewayClient operationalGatewayClient;
  private final OperationalGatewayMapper operationalGatewayMapper;
  private final ApiGeeHeaderSigner apiGeeHeaderSigner;

  public void sendInvoiceCreationResult(ServiceResponse serviceResponse, String invoiceNumber) {
    if (invoiceNumber == null) {
      throw new UnrecoverableApiGeeRequestException("No invoice number was provided.");
    }

    OperationalGatewayRequestPayload requestPayload = operationalGatewayMapper.mapTiServiceResponseToOperationalGatewayPayload(
      serviceResponse, InvoiceNotificationPayload.builder()
        .id("test")
        .buyer("")
        .batchId("")
        .invoiceNumber("")
        .build());

    ApiGeeBaseRequest<OperationalGatewayRequest> requestBody = ApiGeeBaseRequest.<OperationalGatewayRequest>builder()
      .data(OperationalGatewayRequest.builder()
        .referenceData(ReferenceData.builder()
          .provider("Provider 123")
          .correlatedMessageId(UUID.randomUUID().toString())
          .processCode(ProcessCode.builder()
            .code("Process code 123")
            .build())
          .build())
        .payload(requestPayload)
        .build())
      .build();

    Map<String, String> requestHeaders = apiGeeHeaderSigner.buildRequestHeaders(requestBody);

    try {
      operationalGatewayClient.sendInvoiceCreationResult(requestHeaders, requestBody);
      log.info("Invoice creation notified successfully.");
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
