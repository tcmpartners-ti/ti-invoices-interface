package com.tcmp.tiapi.titoapigee.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class EncryptedBodyRequestHeaderSigner extends ApiGeeHeaderSigner implements HeaderSigner {
  private final String device;
  private final String deviceIp;

  public EncryptedBodyRequestHeaderSigner(ObjectMapper objectMapper, String appId, String apiEncryptionKey, String apiSecret, String device, String deviceIp) {
    super(objectMapper, appId, apiEncryptionKey, apiSecret);
    this.device = device;
    this.deviceIp = deviceIp;
  }

  @Override
  public Map<String, String> buildRequestHeaders(ApiGeeBaseRequest<?> baseRequest) {
    String requestId = UUID.randomUUID().toString();
    String session = UUID.randomUUID().toString();
    guid = UUID.randomUUID().toString();

    String signatureHeader;

    try {
      signatureHeader = buildSignatureHeader(baseRequest);
    } catch (JsonProcessingException e) {
      log.error("[ApiGeeRequestHeaderSigner]: Could not sign headers: {}.", e.getMessage());
      throw new UnrecoverableApiGeeRequestException(e.getMessage());
    }

    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
    headers.put("X-Api-Credentials", credentialsHeader);
    headers.put("X-Signature", signatureHeader);
    headers.put("X-Session", session);
    headers.put("X-Request-Id", requestId);
    headers.put("X-Guid", guid);
    headers.put("X-Device", device);
    headers.put("X-Device-Ip", deviceIp);

    return headers;
  }
}
