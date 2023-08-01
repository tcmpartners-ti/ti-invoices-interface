package com.tcmp.tiapi.invoice;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceConfiguration {
    @Bean
    public InvoiceMapper invoiceMapper() {
        return Mappers.getMapper(InvoiceMapper.class);
    }
}
