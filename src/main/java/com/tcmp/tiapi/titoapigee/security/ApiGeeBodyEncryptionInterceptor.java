package com.tcmp.tiapi.titoapigee.security;

import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Slf4j
public class ApiGeeBodyEncryptionInterceptor implements RequestInterceptor {
  private static final String AES_ALGORITHM = "AES";
  private static final String AES_TRANSFORMATION = "AES/CBC/PKCS7Padding";

  private final String apiEncryptionKey;
  private final String apiSecret;

  public ApiGeeBodyEncryptionInterceptor(String apiEncryptionKey, String apiSecret) {
    this.apiEncryptionKey = apiEncryptionKey;
    this.apiSecret = apiSecret;

    Security.addProvider(new BouncyCastleProvider());
  }

  @Override
  public void apply(RequestTemplate requestTemplate) {
    byte[] previousBody = requestTemplate.body();
    String encryptedBody = encryptBodyWithAES(previousBody);

    requestTemplate.body(encryptedBody);
  }

  private String encryptBodyWithAES(byte[] requestBody) {
    try {
      String jsonRequestBody = new String(requestBody, StandardCharsets.UTF_8);

      SecretKeySpec secretKeySpec = new SecretKeySpec(apiEncryptionKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
      byte[] iv = apiSecret.getBytes(StandardCharsets.UTF_8);

      Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION, "BC");
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

      byte[] encryptedBytes = cipher.doFinal(jsonRequestBody.getBytes(StandardCharsets.UTF_8));

      return new String(Base64.encode(encryptedBytes), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException |
             InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
      log.error("Could not encrypt body request. {}.", e.getMessage());
      throw new UnrecoverableApiGeeRequestException("Could not cypher request body.");
    }
  }
}
