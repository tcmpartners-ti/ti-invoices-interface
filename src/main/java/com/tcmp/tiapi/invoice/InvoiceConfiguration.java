package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.route.*;
import com.tcmp.tiapi.invoice.service.InvoiceNotificationCreationService;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import lombok.Getter;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class InvoiceConfiguration {
  @Value("${invoice.route.notification-invoice-creation.from}")
  private String uriNotificationInvoiceCreationFrom;

  @Bean
  public NotificationInvoiceCreationRouteBuilder notificationInvoiceCreationRouteBuilder(
      JaxbDataFormat jaxbDataFormatAckEventRequest,
      InvoiceNotificationCreationService invoiceNotificationCreationService,
      OperationalGatewayService operationalGatewayService) {
    return new NotificationInvoiceCreationRouteBuilder(
        jaxbDataFormatAckEventRequest,
        invoiceNotificationCreationService,
        operationalGatewayService,
        uriNotificationInvoiceCreationFrom);
  }
}
