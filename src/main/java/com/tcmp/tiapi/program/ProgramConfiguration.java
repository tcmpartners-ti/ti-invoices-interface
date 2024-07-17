package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.mapper.ProgramMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgramConfiguration {
  @Bean
  public ProgramMapper programMapper() {
    return Mappers.getMapper(ProgramMapper.class);
  }
}
