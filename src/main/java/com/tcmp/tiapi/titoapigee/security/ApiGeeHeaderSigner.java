package com.tcmp.tiapi.titoapigee.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
public class ApiGeeHeaderSigner {
  private final ObjectMapper objectMapper;

  // Static Data
  private final String appId;
  private final String apiEncryptionKey;
  private final String apiSecret;

  // Data that varies per request
  protected String guid;

  protected String buildCredentialsHeader() {
    String headerPayload = String.format("%s:%s", apiEncryptionKey, apiSecret);
    return Base64.toBase64String(headerPayload.getBytes());
  }

  protected String buildSignatureHeader(ApiGeeBaseRequest<?> baseRequest) throws JsonProcessingException {
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

