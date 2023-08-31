package com.tcmp.tiapi.program.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgramCreatedDTO {
  private final String message;
}
