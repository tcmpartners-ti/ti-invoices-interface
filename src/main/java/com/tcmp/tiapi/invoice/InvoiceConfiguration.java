package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.route.*;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
import com.tcmp.tiapi.invoice.service.InvoiceNotificationCreationService;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import lombok.Getter;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class InvoiceConfiguration {
  @Value("${invoice.route.ack-event-listener.from}")
  private String uriCreatedAckEventFrom;

  @Value("${invoice.route.ack-event-listener.to-finance}")
  private String uriToFinanceFlow;

  @Value("${invoice.route.ack-event-listener.to-settle}")
  private String uriToSettleFlow;

  @Value("${invoice.route.notification-invoice-creation.from}")
  private String uriNotificationInvoiceCreationFrom;

  @Bean
  public InvoiceAckEventListenerRouteBuilder invoiceAckEventListenerRouteBuilder(
      JaxbDataFormat jaxbDataFormatAckEventRequest) {
    return new InvoiceAckEventListenerRouteBuilder(
        jaxbDataFormatAckEventRequest, uriCreatedAckEventFrom, uriToFinanceFlow, uriToSettleFlow);
  }

  @Bean
  public InvoiceFinanceResultFlowRouteBuilder invoiceFinanceResultFlowRouteBuilder(
      InvoiceFinancingService invoiceFinancingService,
      InvoiceSettlementService invoiceSettlementService,
      CorporateLoanService corporateLoanService,
      PaymentExecutionService paymentExecutionService,
      OperationalGatewayService operationalGatewayService,
      BusinessBankingService businessBankingService) {
    return new InvoiceFinanceResultFlowRouteBuilder(
        invoiceFinancingService,
        corporateLoanService,
        paymentExecutionService,
        operationalGatewayService,
        businessBankingService,
        uriToFinanceFlow);
  }

  @Bean
  public InvoiceSettleResultFlowBuilder invoiceSettleResultFlowBuilder(
      InvoiceSettlementService invoiceSettlementService,
      CorporateLoanService corporateLoanService,
      PaymentExecutionService paymentExecutionService,
      OperationalGatewayService operationalGatewayService,
      BusinessBankingService businessBankingService) {
    return new InvoiceSettleResultFlowBuilder(
        invoiceSettlementService,
        corporateLoanService,
        paymentExecutionService,
        operationalGatewayService,
        businessBankingService,
        uriToSettleFlow);
  }

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
