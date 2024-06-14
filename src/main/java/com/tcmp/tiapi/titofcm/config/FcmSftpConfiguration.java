package com.tcmp.tiapi.titofcm.config;

import com.tcmp.tiapi.titofcm.exception.PrivateKeyException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sftp")
public class FcmSftpConfiguration {
  private String user;
  private String host;
  private String port;
  private String privateKey;
  private LocalDir localDir;
  private RemoteDir remoteDir;

  public File privateKeyFile() {
    String privateKeyPEM =
        privateKey
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("----END RSA PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

    try {
      byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      PrivateKey key = keyFactory.generatePrivate(keySpec);

      return getPEMFile(key);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
      throw new PrivateKeyException(e.getMessage());
    }
  }

  public File getPEMFile(PrivateKey privateKey) throws IOException {
    File file = new File(System.getProperty("user.home"), "privatekey.pem");

    try (FileWriter fileWriter = new FileWriter(file);
        PemWriter pemWriter = new PemWriter(fileWriter)) {
      PemObject pemObject = new PemObject("PRIVATE KEY", privateKey.getEncoded());
      pemWriter.writeObject(pemObject);
    }

    return file;
  }

  public String user() {
    return user;
  }

  public String host() {
    return host;
  }

  public String port() {
    return port;
  }

  public LocalDir localDirectories() {
    return localDir;
  }

  public RemoteDir remoteDirectories() {
    return remoteDir;
  }

  @Data
  @AllArgsConstructor
  public static class LocalDir {
    private String fullOutput;
    private String summary;

    public String fullOutput() {
      return fullOutput;
    }

    public String summary() {
      return summary;
    }
  }

  @Data
  @AllArgsConstructor
  public static class RemoteDir {
    private String fullOutput;
    private String summary;

    public String fullOutput() {
      return fullOutput;
    }

    public String summary() {
      return summary;
    }
  }
}
