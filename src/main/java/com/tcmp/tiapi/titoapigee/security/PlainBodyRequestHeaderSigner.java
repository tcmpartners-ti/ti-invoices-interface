package com.tcmp.tiapi.titoapigee.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class PlainBodyRequestHeaderSigner extends ApiGeeHeaderSigner implements HeaderSigner {
  private final String device;
  private final String deviceIp;

  public PlainBodyRequestHeaderSigner(ObjectMapper objectMapper, String appId, String apiEncryptionKey, String apiSecret, String device, String deviceIp) {
    super(objectMapper, appId, apiEncryptionKey, apiSecret);
    this.device = device;
    this.deviceIp = deviceIp;
  }


  @Override
  public Map<String, String> buildRequestHeaders(ApiGeeBaseRequest<?> baseRequest) {
    String session = UUID.randomUUID().toString();
    guid = UUID.randomUUID().toString();

    String signatureHeader;

    try {
      signatureHeader = buildSignatureHeader(baseRequest);
    } catch (JsonProcessingException e) {
      log.error("[ApiGeeRequestHeaderSigner]: Could not sign headers: {}.", e.getMessage());
      throw new UnrecoverableApiGeeRequestException(e.getMessage());
    }

    return Map.ofEntries(
      Map.entry(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE),
      Map.entry("X-Apigee-Credentials", credentialsHeader),
      Map.entry("X-Signature", signatureHeader),
      Map.entry("X-Session", session),
      Map.entry("X-Guid", guid),
      Map.entry("X-Device", device),
      Map.entry("X-Device-IP", deviceIp)
    );
  }
}
