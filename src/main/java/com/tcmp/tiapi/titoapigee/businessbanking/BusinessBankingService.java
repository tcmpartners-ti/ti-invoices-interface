package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.ProcessCode;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.ReferenceData;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessBankingService {
  private static final int MAX_ATTEMPTS = 4;
  private static final String REQUEST_PROVIDER = "FTI";

  private final HeaderSigner encryptedBodyRequestHeaderSigner;
  private final BusinessBankingClient businessBankingClient;
  private final UUIDGenerator uuidGenerator;

  @Retryable(
      retryFor = {
        FeignException.InternalServerError.class,
        FeignException.GatewayTimeout.class,
        FeignException.NotFound.class // Somehow the service throws this error.
      },
      maxAttempts = MAX_ATTEMPTS,
      backoff = @Backoff(delay = 1_000))
  public void notifyEvent(OperationalGatewayProcessCode processCode, Object payload) {
    ApiGeeBaseRequest<OperationalGatewayRequest<?>> body =
        ApiGeeBaseRequest.<OperationalGatewayRequest<?>>builder()
            .data(
                OperationalGatewayRequest.builder()
                    .referenceData(
                        ReferenceData.builder()
                            .provider(REQUEST_PROVIDER)
                            .correlatedMessageId(uuidGenerator.getNewId())
                            .processCode(ProcessCode.of(processCode))
                            .build())
                    .payload(payload)
                    .build())
            .build();

    Map<String, String> headers = encryptedBodyRequestHeaderSigner.buildRequestHeaders(body);

    try {
      businessBankingClient.notifyEvent(headers, body);
      log.info("Event notified. {}", body);
    } catch (FeignException e) {
      RetryContext context = RetrySynchronizationManager.getContext();
      if (context != null) {
        int currentAttempts = context.getRetryCount() + 1;
        log.warn("Retrying event notification. Attempt {} of {}.", currentAttempts, MAX_ATTEMPTS);
      }

      // Spread exception to let Retryable handle retries.
      throw e;
    }
  }
}
