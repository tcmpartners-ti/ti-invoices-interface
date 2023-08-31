package com.tcmp.tiapi.program;

import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.router.processor.XmlNamespaceFixer;
import lombok.Getter;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@EnableConfigurationProperties
public class ProgramConfiguration {

  @Value("${program.route.create.from}")
  private String uriCreateFrom;

  @Value("${program.route.create.to}")
  private String uriCreateTo;

  @Bean
  public ProgramMapper programMapper() {
    return Mappers.getMapper(ProgramMapper.class);
  }

  @Bean
  CreateProgramRouteBuilder programRouter(JaxbDataFormat jaxbDataFormat) {
    return new CreateProgramRouteBuilder(
      new TIServiceRequestWrapper(),
      jaxbDataFormat,
      new XmlNamespaceFixer(),
      uriCreateFrom,
      uriCreateTo
    );
  }
}
