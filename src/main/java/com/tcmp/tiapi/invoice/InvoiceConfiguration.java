package com.tcmp.tiapi.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.invoice.route.BulkCreateInvoicesRouteBuilder;
import com.tcmp.tiapi.invoice.route.CreateInvoiceRouteBuilder;
import com.tcmp.tiapi.invoice.route.InvoiceCreatedEventListenerRouteBuilder;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import com.tcmp.tiapi.titoapigee.service.OperationalGatewayService;
import lombok.Getter;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Qualifier;
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

  @Value("${invoice.route.creation-listener.from}")
  private String uriCreatedEventFrom;
  @Value("${invoice.route.creation-listener.to}")
  private String uriCreatedEventTo;

  @Bean
  public CreateInvoiceRouteBuilder invoiceRouter(
    @Qualifier("jaxbDataFormatServiceRequest")
    JaxbDataFormat jaxbDataFormatServiceRequest
  ) {
    return new CreateInvoiceRouteBuilder(
      jaxbDataFormatServiceRequest,
      new TIServiceRequestWrapper(),
      new XmlNamespaceFixer(),
      uriCreateFrom,
      uriCreateTo
    );
  }

  @Bean
  public BulkCreateInvoicesRouteBuilder bulkCreateInvoicesRouter(
    @Qualifier("jaxbDataFormatServiceRequest")
    JaxbDataFormat jaxbDataFormatServiceRequest,
    InvoiceMapper invoiceMapper
  ) {
    return new BulkCreateInvoicesRouteBuilder(
      jaxbDataFormatServiceRequest,
      new ObjectMapper(),
      invoiceMapper,
      new TIServiceRequestWrapper(),
      new XmlNamespaceFixer(),
      uriBulkCreateFrom,
      uriBulkCreateTo
    );
  }

  @Bean
  public InvoiceCreatedEventListenerRouteBuilder invoiceCreatedEventListenerRouteBuilder(
    @Qualifier("jaxbDataFormatServiceResponse")
    JaxbDataFormat jaxbDataFormatServiceResponse,
    OperationalGatewayService operationalGatewayService
  ) {
    return new InvoiceCreatedEventListenerRouteBuilder(
      jaxbDataFormatServiceResponse,
      new ObjectMapper(),
      operationalGatewayService,
      uriCreatedEventFrom,
      uriCreatedEventTo
    );
  }
}
