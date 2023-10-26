package com.tcmp.tiapi.titoapigee.operationalgateway;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.request.*;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.Channel;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.NotificationsResponse;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.EmailNotFoundException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationalGatewayService {
  private final HeaderSigner operationalGatewayHeaderSigner;
  private final OperationalGatewayClient operationalGatewayClient;

  public void sendEmailNotification(
    String customerMnemonic,
    String customerEmail,
    String templateId,
    List<TemplateField> fields
  ) {
    if (customerEmail == null || customerEmail.isBlank()) {
      throw new EmailNotFoundException("Email was not provided, could not send notification.");
    }

    NotificationsRequest requestData = NotificationsRequest.builder()
      .flow(new Flow("notificacionesAdicionales"))
      .requester(new Requester(customerMnemonic, "001"))
      .additionalRecipient(List.of(
        new Recipient(
          new Email(customerEmail),
          new Cellphone(" ", " ")
        )))
      .template(Template.builder()
        .templateId(templateId)
        .sequentialId("0")
        .fields(fields)
        .build())
      .build();
    ApiGeeBaseRequest<NotificationsRequest> request = ApiGeeBaseRequest.<NotificationsRequest>builder()
      .data(requestData)
      .build();

    Map<String, String> headers = operationalGatewayHeaderSigner.buildRequestHeaders(request);

    try {
      NotificationsResponse result = operationalGatewayClient.sendEmailNotification(headers, request);
      Channel channel = result.data().get(0).recipient().channel();
      log.info("Successfully sent notification via {} to: {}", channel.description(), channel.value());
    } catch (FeignException e) {
      log.error("Could not send email.");
    }
  }
}
