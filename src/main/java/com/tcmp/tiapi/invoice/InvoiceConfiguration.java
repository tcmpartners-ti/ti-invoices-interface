package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.route.BulkCreateInvoicesRouteBuilder;
import com.tcmp.tiapi.invoice.route.CreateInvoiceRouteBuilder;
import com.tcmp.tiapi.invoice.route.InvoiceCreatedEventListenerRouteBuilder;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.Getter;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.mapstruct.factory.Mappers;
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
  public InvoiceMapper invoiceMapper() {
    return Mappers.getMapper(InvoiceMapper.class);
  }

  @Bean
  public CreateInvoiceRouteBuilder invoiceRouter(JaxbDataFormat jaxbDataFormat) {
    return new CreateInvoiceRouteBuilder(
      jaxbDataFormat,
      new TIServiceRequestWrapper(),
      new XmlNamespaceFixer(),
      uriCreateFrom,
      uriCreateTo
    );
  }

  @Bean
  public BulkCreateInvoicesRouteBuilder bulkCreateInvoicesRouter(JaxbDataFormat jaxbDataFormat, InvoiceMapper invoiceMapper) {
    return new BulkCreateInvoicesRouteBuilder(
      jaxbDataFormat,
      invoiceMapper,
      new TIServiceRequestWrapper(),
      new XmlNamespaceFixer(),
      uriBulkCreateFrom,
      uriBulkCreateTo
    );
  }

  @Bean
  public InvoiceCreatedEventListenerRouteBuilder invoiceCreatedEventListenerRouteBuilder() {
    return new InvoiceCreatedEventListenerRouteBuilder(
      uriCreatedEventFrom,
      uriCreatedEventTo
    );
  }
}
