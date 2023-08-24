package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.router.BulkCreateInvoicesRouter;
import com.tcmp.tiapi.invoice.router.CreateInvoiceRouter;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
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
  @Value("${invoice.route.create.to-pub}")
  private String uriCreateToPub;
  @Value("${invoice.route.create.to-sub}")
  private String uriCreateToSub;

  @Value("${invoice.route.create-bulk.from}")
  private String uriBulkCreateFrom;
  @Value("${invoice.route.create-bulk.to}")
  private String uriBulkCreateTo;

  @Bean
  public InvoiceMapper invoiceMapper() {
    return Mappers.getMapper(InvoiceMapper.class);
  }

  @Bean
  public CreateInvoiceRouter invoiceRouter(JaxbDataFormat jaxbDataFormat) {
    return new CreateInvoiceRouter(
      jaxbDataFormat,
      new TIServiceRequestWrapper(),
      uriCreateFrom,
      uriCreateToPub,
      uriCreateToSub
    );
  }

  @Bean
  public BulkCreateInvoicesRouter bulkCreateInvoicesRouter(JaxbDataFormat jaxbDataFormat, InvoiceMapper invoiceMapper) {
    return new BulkCreateInvoicesRouter(
      jaxbDataFormat,
      new TIServiceRequestWrapper(),
      invoiceMapper,
      uriBulkCreateFrom,
      uriBulkCreateTo
    );
  }
}
