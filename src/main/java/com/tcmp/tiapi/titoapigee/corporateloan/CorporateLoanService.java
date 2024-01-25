package com.tcmp.tiapi.titoapigee.corporateloan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import jakarta.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporateLoanService {
  private final ObjectMapper objectMapper;
  private final CorporateLoanClient corporateLoanClient;
  private final HeaderSigner encryptedBodyRequestHeaderSigner;

  @Value("${bp.api-gee.services.corporate-loan.user}")
  private String userHeader;

  public DistributorCreditResponse createCredit(DistributorCreditRequest distributorCreditRequest) {
    return createCredit(distributorCreditRequest, false);
  }

  public DistributorCreditResponse simulateCredit(
      DistributorCreditRequest distributorCreditRequest) {
    return createCredit(distributorCreditRequest, true);
  }

  /**
   * This method creates a credit in GAF. If there's an error, throws an exception.
   *
   * @param distributorCreditRequest The request body.
   * @param isSimulation Used to tell GAF if the operation should impact as a real operation or not.
   *     Used when we want to ask GAF the values for the seller taxes and solca.
   * @return The operation result.
   */
  private DistributorCreditResponse createCredit(
      DistributorCreditRequest distributorCreditRequest, boolean isSimulation) {
    ApiGeeBaseRequest<DistributorCreditRequest> request =
        ApiGeeBaseRequest.<DistributorCreditRequest>builder()
            .data(distributorCreditRequest)
            .build();

    Map<String, String> headers = encryptedBodyRequestHeaderSigner.buildRequestHeaders(request);
    // Add missing headers for this service
    headers.put("X-User", userHeader);
    headers.put("X-Operation-Token", buildOperationId());
    headers.put("X-Operation-Id", isSimulation ? "V/X" : "C/D");

    DistributorCreditResponse response = null;

    try {
      response = corporateLoanClient.createCredit(headers, request);
      log.info(
          "Credit created. Amount $ {}. Disbursement Amount $ {}.",
          distributorCreditRequest.amount(),
          response.data().disbursementAmount());

      return response;
    } catch (FeignException e) {
      log.error(e.getMessage());

      e.responseBody()
          .ifPresent(
              errorBytes -> {
                String error = new String(errorBytes.array(), StandardCharsets.UTF_8);
                log.error("Body={}", error);
              });

      throw new CreditCreationException("Credit creation failed.");
    } finally {
      tryRequestAndResponseLogging(headers, request, response);
    }
  }

  private String buildOperationId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
  }

  private void tryRequestAndResponseLogging(
      Map<String, String> headers, ApiGeeBaseRequest<?> request, @Nullable Object response) {
    try {
      log.info("Headers={}", objectMapper.writeValueAsString(headers));
      log.info("Request={}", objectMapper.writeValueAsString(request));
      log.info("Response={}", response == null ? "-" : objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      log.error("Could not log request and response json.");
    }
  }
}
