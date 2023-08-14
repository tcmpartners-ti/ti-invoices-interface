package com.tcmp.tiapi.invoice;

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

  @Bean
  public InvoiceMapper invoiceMapper() {
    return Mappers.getMapper(InvoiceMapper.class);
  }

  @Bean
  public InvoiceRouter invoiceRouter() {
    return new InvoiceRouter(
      uriCreateFrom,
      uriCreateTo
    );
  }
}
