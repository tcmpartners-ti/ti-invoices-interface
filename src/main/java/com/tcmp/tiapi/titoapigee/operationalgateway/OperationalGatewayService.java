package com.tcmp.tiapi.titoapigee.operationalgateway;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.request.*;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.Channel;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.NotificationsResponse;
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
    String requesterDocumentNumber,
    String recipientEmail,
    String recipientName,
    String invoiceReference
  ) {
    NotificationsRequest requestData = NotificationsRequest.builder()
      .flow(new Flow("notificacionesAdicionales"))
      .requester(new Requester(requesterDocumentNumber, "001"))
      .additionalRecipient(List.of(
        new Recipient(
          new Email(recipientEmail),
          new Cellphone(" ", " ")
        )))
      .template(Template.builder()
        .templateId("202300701")
        .sequentialId("0")
        .fields(buildEmailTemplate(recipientName, invoiceReference))
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

  private List<TemplateField> buildEmailTemplate(
    String recipientName,
    String invoiceReference
  ) {
    return List.of(
      new TemplateField(
        "motivo",
        """
          <p style="text-align: justify;"> Estimado/a <strong>%s</strong>, </p>
          <p style="text-align: justify;">Se ha emitido un evento de descuento de factura.</p>
          """.formatted(recipientName)),
      new TemplateField(
        "informacion",
        """
          <p style="text-align: justify;">La factura <strong># %s</strong> ha sido descontada.</p>
           """.formatted(invoiceReference)
      ),
      new TemplateField("url", "https://desarrollo-portalpagos.pichincha.com/#/home/electronic/file/https://desarrollo-portalpagos.pichincha.com"),
      new TemplateField("img-background", "https://desarrollo-portalpagos.pichincha.com/assets/email/background.png"),
      new TemplateField("img-notification", "https://desarrollo-portalpagos.pichincha.com/assets/email/notification.jpg")
    );
  }
}
