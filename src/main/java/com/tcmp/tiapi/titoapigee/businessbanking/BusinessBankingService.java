package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.invoice.dto.request.InvoiceNotificationPayload;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.ProcessCode;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.ReferenceData;
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

  public void sendInvoiceCreationResult(ServiceResponse serviceResponse, String invoiceNumber) {
    if (invoiceNumber == null) {
      throw new UnrecoverableApiGeeRequestException("No invoice number was provided.");
    }

    OperationalGatewayRequestPayload requestPayload = businessBankingMapper.mapTiServiceResponseToOperationalGatewayPayload(
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
      businessBankingClient.sendInvoiceCreationResult(requestHeaders, requestBody);
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
