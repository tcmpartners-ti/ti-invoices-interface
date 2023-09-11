package com.tcmp.tiapi.shared.dto.response.error;

public record SimpleHttpErrorMessage(
  int status,
  String error
) {
}
