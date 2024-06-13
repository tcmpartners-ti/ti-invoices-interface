package com.tcmp.tiapi.titoapigee.operationalgateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.request.*;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.Channel;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.NotificationsResponse;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import feign.FeignException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationalGatewayService {
  private final ObjectMapper objectMapper;
  private final HeaderSigner plainBodyRequestHeaderSigner;
  private final OperationalGatewayClient operationalGatewayClient;

  @Value("${bp.service.operational-gateway.business-banking-url}")
  private String businessBankingUrl;

  @Value("${bp.service.operational-gateway.template-id}")
  private String templateId;

  public void sendNotificationRequest(InvoiceEmailInfo emailInfo) {
    NotificationsRequest notificationsRequest =
        NotificationsRequest.builder()
            .flow(new Flow("notificacionesAdicionales"))
            .requester(new Requester(emailInfo.customerMnemonic(), "001"))
            .additionalRecipient(
                List.of(
                    new Recipient(new Email(emailInfo.customerEmail()), new Cellphone(" ", " "))))
            .template(
                Template.builder()
                    .templateId(templateId)
                    .sequentialId("0")
                    .fields(
                        buildInvoiceEventEmailTemplate(
                            emailInfo.customerMnemonic(),
                            emailInfo.customerName(),
                            emailInfo.date(),
                            emailInfo.action(),
                            emailInfo.invoiceNumber(),
                            emailInfo.invoiceCurrency(),
                            emailInfo.amount()))
                    .build())
            .build();

    ApiGeeBaseRequest<NotificationsRequest> request =
        ApiGeeBaseRequest.<NotificationsRequest>builder().data(notificationsRequest).build();
    Map<String, String> headers = plainBodyRequestHeaderSigner.buildRequestHeaders(request);

    try {
      NotificationsResponse response =
          operationalGatewayClient.sendEmailNotification(headers, request);
      Channel channel = response.data().get(0).recipient().channel();
      log.info(
          "Successfully sent notification via {} to: {}", channel.description(), channel.value());
      tryRequestAndResponseLogging(request, response);
    } catch (FeignException e) {
      log.error("Failed to send email notification: {}", e.getMessage());
    }
  }

  private List<TemplateField> buildInvoiceEventEmailTemplate(
      String customerMnemonic,
      String customerName,
      String date,
      String action,
      String invoiceReference,
      String currency,
      BigDecimal amount) {
    DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

    int unmaskedDigitsIndex = customerMnemonic.length() - 7;
    String maskedMnemonic =
        "x".repeat(unmaskedDigitsIndex) + customerMnemonic.substring(unmaskedDigitsIndex);
    String message =
        String.format(
            "%s de la factura No. %s por %s %s",
            action, invoiceReference, currency, decimalFormat.format(amount));

    return List.of(
        new TemplateField("RucEnmascarado", maskedMnemonic),
        new TemplateField("NombreEmpresa", customerName),
        new TemplateField("FechaIngreso", date),
        new TemplateField("HoraIngreso", "12:00"),
        new TemplateField("action", message),
        new TemplateField("urlBanca", businessBankingUrl));
  }

  private void tryRequestAndResponseLogging(ApiGeeBaseRequest<?> request, Object response) {
    try {
      log.info("Request={}", objectMapper.writeValueAsString(request));
      log.info("Response={}", objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      log.error("Could not log request and response json.");
    }
  }
}
