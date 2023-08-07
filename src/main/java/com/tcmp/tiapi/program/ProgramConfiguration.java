package com.tcmp.tiapi.program;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgramConfiguration {
    @Bean
    public ProgramMapper programMapper() {
        return Mappers.getMapper(ProgramMapper.class);
    }

    @Bean
    ProgramRouter programRouter(
        JaxbDataFormat jaxbDataFormat,
        @Value("${route.program.create.single.from}") String uriFrom,
        @Value("${route.program.create.single.to}") String uriTo
    ) {
        return new ProgramRouter(jaxbDataFormat, uriFrom, uriTo);
    }
}
