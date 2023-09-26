package com.tcmp.tiapi.titoapigee.businessbanking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.security.ApiGeeHeaderSigner;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

@Qualifier("businessBankingHeaderSigner")
@Slf4j
public class BusinessBankingHeaderSigner extends ApiGeeHeaderSigner implements HeaderSigner {
  private final String device;
  private final String deviceIp;

  public BusinessBankingHeaderSigner(ObjectMapper objectMapper, String appId, String apiEncryptionKey, String apiSecret, String device, String deviceIp) {
    super(objectMapper, appId, apiEncryptionKey, apiSecret);
    this.device = device;
    this.deviceIp = deviceIp;
  }

  @Override
  public Map<String, String> buildRequestHeaders(ApiGeeBaseRequest<?> baseRequest) {
    String requestId = UUID.randomUUID().toString();
    String session = UUID.randomUUID().toString();
    guid = UUID.randomUUID().toString();

    String credentialsHeader = buildCredentialsHeader();
    String signatureHeader;

    try {
      signatureHeader = buildSignatureHeader(baseRequest);
    } catch (JsonProcessingException e) {
      log.error("[ApiGeeRequestHeaderSigner]: Could not sign headers: {}.", e.getMessage());
      throw new UnrecoverableApiGeeRequestException(e.getMessage());
    }

    return Map.ofEntries(
      Map.entry(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE),
      Map.entry("X-Api-Credentials", credentialsHeader),
      Map.entry("X-Signature", signatureHeader),
      Map.entry("X-Session", session),
      Map.entry("X-Request-Id", requestId),
      Map.entry("X-Guid", guid),
      Map.entry("X-Device", device),
      Map.entry("X-Device-Ip", deviceIp)
    );
  }
}
