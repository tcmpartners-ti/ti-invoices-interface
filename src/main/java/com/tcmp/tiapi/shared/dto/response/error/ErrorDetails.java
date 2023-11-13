package com.tcmp.tiapi.shared.dto.response.error;

import java.io.Serial;
import java.io.Serializable;

public record ErrorDetails(
  String field,
  String error
) implements Serializable {
  @Serial private static final long serialVersionUID = 9898273489719L;
}
