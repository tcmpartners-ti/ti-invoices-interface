package com.tcmp.tiapi.titoapigee.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
public class ApiGeeHeaderSigner {
  private final ObjectMapper objectMapper;

  // Static Data
  private final String appId;
  private final String apiEncryptionKey;
  private final String apiSecret;
  protected String credentialsHeader;

  // Data that varies per request
  protected String guid;

  public ApiGeeHeaderSigner(ObjectMapper objectMapper, String appId, String apiEncryptionKey, String apiSecret) {
    this.objectMapper = objectMapper;
    this.appId = appId;
    this.apiEncryptionKey = apiEncryptionKey;
    this.apiSecret = apiSecret;

    credentialsHeader = buildCredentialsHeader();
  }

  private String buildCredentialsHeader() {
    String headerPayload = String.format("%s:%s", apiEncryptionKey, apiSecret);
    return Base64.encodeBase64String(headerPayload.getBytes());
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
    byte[] hashedInput = DigestUtils.sha256(value);
    return Base64.encodeBase64String(hashedInput);
  }
}
