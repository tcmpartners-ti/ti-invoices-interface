package com.tcmp.tiapi.titoapigee.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpStatus;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Slf4j
public class ApiGeeResponseBodyDecoder implements Decoder {
  private static final String AES_ALGORITHM = "AES";
  private static final String AES_TRANSFORMATION = "AES/CBC/PKCS7Padding";

  private final ObjectMapper objectMapper;
  private final String apiEncryptionKey;
  private final String apiSecret;

  public ApiGeeResponseBodyDecoder(ObjectMapper objectMapper, String apiEncryptionKey, String apiSecret) {
    this.objectMapper = objectMapper;
    this.apiEncryptionKey = apiEncryptionKey;
    this.apiSecret = apiSecret;

    Security.addProvider(new BouncyCastleProvider());
  }

  @Override
  public Object decode(Response response, Type type) throws IOException, FeignException {
    byte[] responseBodyBytes = response.body().asInputStream().readAllBytes();
    String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

    // Response body is only encrypted when status is OK.
    if (response.status() == HttpStatus.CREATED.value()) {
      responseBody = decryptWithAES(responseBody)
        .replace("\\", "")
        .replaceAll("^\"|^\"$", "");
    }

    return objectMapper.readValue(responseBody, DistributorCreditResponse.class);
  }

  private String decryptWithAES(String base64Encoded) {
    try {
      byte[] encryptedBytes = Base64.decode(base64Encoded);

      SecretKeySpec secretKeySpec = new SecretKeySpec(apiEncryptionKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
      byte[] iv = apiSecret.getBytes(StandardCharsets.UTF_8);
      Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION, "BC");
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

      byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
      return new String(decryptedBytes, StandardCharsets.UTF_8);

    } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException |
             InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
             BadPaddingException e) {
      throw new UnrecoverableApiGeeRequestException("Could not decrypt response body.");
    }
  }
}
