package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.route.*;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
import com.tcmp.tiapi.invoice.service.InvoiceNotificationCreationService;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.invoice.validation.InvoiceRowValidator;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
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
  @Value("${invoice.route.create.from}")
  private String uriCreateFrom;

  @Value("${invoice.route.create.to}")
  private String uriCreateTo;

  @Value("${invoice.route.create-bulk.from}")
  private String uriBulkCreateFrom;

  @Value("${invoice.route.create-bulk.to}")
  private String uriBulkCreateTo;

  @Value("${invoice.route.event-listener.max-retries}")
  private Integer maxRetries;

  @Value("${invoice.route.event-listener.retry-delay}")
  private Integer retryDelayInMs;

  @Value("${invoice.route.event-listener.from}")
  private String uriCreatedEventFrom;

  @Value("${invoice.route.event-listener.to}")
  private String uriCreatedEventTo;

  @Value("${invoice.route.finance.from}")
  private String uriFinanceFrom;

  @Value("${invoice.route.finance.to}")
  private String uriFinanceTo;

  @Value("${invoice.route.ack-event-listener.from}")
  private String uriCreatedAckEventFrom;

  @Value("${invoice.route.ack-event-listener.to-finance}")
  private String uriToFinanceFlow;

  @Value("${invoice.route.ack-event-listener.to-settle}")
  private String uriToSettleFlow;

  @Value("${invoice.route.notification-invoice-creation.from}")
  private String uriNotificationInvoiceCreationFrom;

  @Bean
  public CreateInvoiceRouteBuilder createInvoiceRouter(
      InvoiceEventService invoiceEventService,
      JaxbDataFormat jaxbDataFormatServiceRequest,
      InvoiceMapper invoiceMapper) {
    return new CreateInvoiceRouteBuilder(
        invoiceEventService,
        jaxbDataFormatServiceRequest,
        new TIServiceRequestWrapper(),
        new XmlNamespaceFixer(),
        uriCreateFrom,
        uriCreateTo);
  }

  @Bean
  public BulkCreateInvoicesRouteBuilder bulkCreateInvoicesRouter(InvoiceMapper invoiceMapper) {
    return new BulkCreateInvoicesRouteBuilder(invoiceMapper, uriBulkCreateFrom, uriBulkCreateTo);
  }

  @Bean
  public InvoiceEventListenerRouteBuilder invoiceEventListenerRouteBuilder(
      JaxbDataFormat jaxbDataFormatServiceResponse,
      InvoiceEventService invoiceEventService,
      BusinessBankingService businessBankingService) {
    return new InvoiceEventListenerRouteBuilder(
        jaxbDataFormatServiceResponse,
        invoiceEventService,
        businessBankingService,
        uriCreatedEventFrom,
        uriCreatedEventTo,
        maxRetries,
        retryDelayInMs);
  }

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
  public FinanceInvoiceRouteBuilder financeInvoiceRouteBuilder(
      InvoiceEventService invoiceEventService, JaxbDataFormat jaxbDataFormatServiceRequest) {
    return new FinanceInvoiceRouteBuilder(
        invoiceEventService,
        new TIServiceRequestWrapper(),
        jaxbDataFormatServiceRequest,
        new XmlNamespaceFixer(),
        uriFinanceFrom,
        uriFinanceTo);
  }

  @Bean
  public InvoiceRowValidator invoiceRowValidator() {
    return new InvoiceRowValidator();
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
