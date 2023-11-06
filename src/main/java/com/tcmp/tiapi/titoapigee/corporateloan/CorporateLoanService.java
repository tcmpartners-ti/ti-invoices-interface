package com.tcmp.tiapi.titoapigee.corporateloan;

import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporateLoanService {
  private final CorporateLoanClient corporateLoanClient;
  private final HeaderSigner encryptedBodyRequestHeaderSigner;

  @Value("${bp.api-gee.services.corporate-loan.user}") private String userHeader;
  @Value("${bp.api-gee.services.corporate-loan.operation-token}") private String operationTokenHeader;

  public DistributorCreditResponse createCredit(DistributorCreditRequest distributorCreditRequest) {
    ApiGeeBaseRequest<DistributorCreditRequest> body = ApiGeeBaseRequest.<DistributorCreditRequest>builder()
      .data(distributorCreditRequest)
      .build();

    Map<String, String> headers = encryptedBodyRequestHeaderSigner.buildRequestHeaders(body);
    // Add missing headers for this service
    headers.put("X-User", userHeader);
    headers.put("X-Operation-Token", operationTokenHeader);
    headers.put("X-Operation-Id", "C/D");

    try {
      return corporateLoanClient.createCredit(headers, body);
    } catch (FeignException e) {
      log.error("Could not create credit.");
      e.responseBody().ifPresent(errorBytes -> {
        String error = new String(errorBytes.array(), StandardCharsets.UTF_8);
        log.info("Body={}", error);
      });

      throw e;
    }
  }
}
