package com.tcmp.tiapi.titofcm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.titofcm.client.SingleElectronicPaymentClient;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import feign.FeignException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SingleElectronicPaymentService {
  private final ObjectMapper objectMapper;
  private final UUIDGenerator uuidGenerator;
  private final SingleElectronicPaymentClient client;

  @Value("${fcm.api.headers.user-id}")
  private String userIdHeader;

  public SinglePaymentResponse createSinglePayment(SinglePaymentRequest request)
      throws SinglePaymentException {
    try {
      Map<String, String> headers =
          Map.ofEntries(
              Map.entry("X-Request-ID", generateRequestId()),
              Map.entry("Accept", MediaType.APPLICATION_JSON_VALUE),
              Map.entry("Content-Type", MediaType.APPLICATION_JSON_VALUE),
              Map.entry("_USERID", userIdHeader));

      SinglePaymentResponse response = client.createSinglePayment(headers, request);
      logRequestAndResponse(request, response);

      return response;
    } catch (FeignException e) {
      logRequestAndResponse(request, null);
      throw new SinglePaymentException(e.getMessage(), e);
    }
  }

  private String generateRequestId() {
    return uuidGenerator.getNewId().replace("-", "").substring(0, 20);
  }

  private void logRequestAndResponse(SinglePaymentRequest request, SinglePaymentResponse response) {
    try {
      log.info("Request={}", objectMapper.writeValueAsString(request));
      String responseResult = response == null ? "-" : objectMapper.writeValueAsString(response);
      log.info("Response={}", responseResult);
    } catch (JsonProcessingException e) {
      log.error("Could not log request and response json.");
    }
  }
}
