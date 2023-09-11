package com.tcmp.tiapi.titoapigee.service;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCorrelationPayload;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.client.OperationalGatewayClient;
import com.tcmp.tiapi.titoapigee.dto.request.*;
import com.tcmp.tiapi.titoapigee.mapper.OperationalGatewayMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationalGatewayService {
  private final OperationalGatewayClient operationalGatewayClient;
  private final OperationalGatewayMapper operationalGatewayMapper;

  public void sendInvoiceCreationResult(ServiceResponse serviceResponse, InvoiceCorrelationPayload invoiceInfo) {
    OperationalGatewayRequestPayload requestPayload = operationalGatewayMapper.mapTiServiceResponseToOperationalGatewayPayload(
      serviceResponse, invoiceInfo);

    OperationalGatewayRequest request = OperationalGatewayRequest.builder()
      .data(OperationalGatewayRequestData.builder()
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

      Object response = operationalGatewayClient.sendInvoiceCreationResult(request);

    log.info("Service response: {}", response);
  }
}
