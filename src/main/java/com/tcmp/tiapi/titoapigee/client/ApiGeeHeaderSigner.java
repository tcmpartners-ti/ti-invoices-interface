package com.tcmp.tiapi.titoapigee.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class ApiGeeHeaderSigner {
  private final ObjectMapper objectMapper;

  // Static Data
  private final String device;
  private final String deviceIp;
  private final String session;
  private final String appId;
  private final String apiEncryptionKey;
  private final String apiSecret;

  // Data that varies per request
  private String guid;

  public Map<String, String> buildRequestHeaders(ApiGeeBaseRequest<?> baseRequest) {
    String requestId = UUID.randomUUID().toString();
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

      Map.entry("X-Device", device),
      Map.entry("X-Device-Ip", deviceIp),

      Map.entry("X-Request-Id", requestId),
      Map.entry("X-Session", session),
      Map.entry("X-Guid", guid)
    );
  }

  private String buildCredentialsHeader() {
    String headerPayload = String.format("%s:%s", apiEncryptionKey, apiSecret);
    return Base64.toBase64String(headerPayload.getBytes());
  }

  private String buildSignatureHeader(ApiGeeBaseRequest<?> baseRequest) throws JsonProcessingException {
    Object requestBodyData = baseRequest.data();
    String dataJson = objectMapper.writeValueAsString(requestBodyData);
    String signatureContent = getSignaturePayload(dataJson);

    return signValue(signatureContent);
  }

  private String getSignaturePayload(String jsonPayload) {
    return String.format("%s|%s|%s|%s", appId, guid, jsonPayload, apiSecret);
  }

  private String signValue(String value) {
    byte[] hashedInput = getSHA256Digest(value);
    return Base64.toBase64String(hashedInput);
  }

  private byte[] getSHA256Digest(String input) {
    SHA256Digest digest = new SHA256Digest();
    byte[] message = input.getBytes(StandardCharsets.UTF_8);
    byte[] hash = new byte[digest.getDigestSize()];

    digest.update(message, 0, message.length);
    digest.doFinal(hash, 0);

    return hash;
  }
}
