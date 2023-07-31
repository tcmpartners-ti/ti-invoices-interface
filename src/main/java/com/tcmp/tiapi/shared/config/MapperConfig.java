package com.tcmp.tiapi.shared.config;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.program.ProgramMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public InvoiceMapper invoiceMapper() {
        return Mappers.getMapper(InvoiceMapper.class);
    }

    @Bean
    public ProgramMapper programMapper() {
        return Mappers.getMapper(ProgramMapper.class);
    }
}
