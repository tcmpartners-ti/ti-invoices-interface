package com.tcmp.tiapi.titofcm.route;

import com.tcmp.tiapi.titofcm.config.FcmSftpConfiguration;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class FcmSftpRouteBuilder extends RouteBuilder {
  private final String uriFromFullOutput;
  private final String uriFromSummary;

  private final FcmSftpConfiguration sftp;

  @Override
  public void configure() throws URISyntaxException {
    String uriToFullOutput = buildSftpFolderUri(sftp.remoteDirectories().fullOutput());
    String uriToSummary = buildSftpFolderUri(sftp.remoteDirectories().summary());

    from(uriFromFullOutput)
        .routeId("sendFullOutputToFcmViaSftp")
        .transform()
        .body(String.class, this::readFileFromPath)
        .to(uriToFullOutput)
        .log("Full output file uploaded successfully.")
        .end();

    from(uriFromSummary)
        .routeId("sendSummaryToFcmViaSftp")
        .transform()
        .body(String.class, this::readFileFromPath)
        .to(uriToSummary)
        .log("Summary file uploaded successfully.")
        .end();
  }

  private String buildSftpFolderUri(String remoteDir) throws URISyntaxException {
    String baseSftpUri = String.format("sftp://%s@%s:%s", sftp.user(), sftp.host(), sftp.port());

    return UriComponentsBuilder.newInstance()
        .uri(new URI(baseSftpUri))
        .path(remoteDir)
        .queryParam("privateKeyFile", sftp.privateKeyFile())
        .queryParam("useUserKnownHostsFile", false)
        .queryParam("stepwise", false)
        .queryParam("streamDownload", true)
        .build()
        .toString();
  }

  /**
   * @param filePath Path to open the file, located in the route body.
   * @return The file contents
   */
  private InputStream readFileFromPath(String filePath) {
    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
      StringBuilder fileContent = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        fileContent.append(line).append(System.lineSeparator());
      }

      return new ByteArrayInputStream(fileContent.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }
}
