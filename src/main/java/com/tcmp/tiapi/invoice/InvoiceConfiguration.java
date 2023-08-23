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

  @Value("${invoice.route.create.to-pub}")
  private String uriCreateToPub;

  @Value("${invoice.route.create.to-sub}")
  private String uriCreateToSub;

  @Bean
  public InvoiceMapper invoiceMapper() {
    return Mappers.getMapper(InvoiceMapper.class);
  }

  @Bean
  public InvoiceRouter invoiceRouter(JaxbDataFormat jaxbDataFormat) {
    return new InvoiceRouter(
      jaxbDataFormat,
      uriCreateFrom,
      uriCreateToPub,
      uriCreateToSub
    );
  }
}
