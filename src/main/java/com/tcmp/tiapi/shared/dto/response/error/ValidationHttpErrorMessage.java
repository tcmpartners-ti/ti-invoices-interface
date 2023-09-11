package com.tcmp.tiapi.shared.dto.response.error;

import java.util.List;

public record ValidationHttpErrorMessage(
  int status,
  String error,
  List<ErrorDetails> errors
) {
}
