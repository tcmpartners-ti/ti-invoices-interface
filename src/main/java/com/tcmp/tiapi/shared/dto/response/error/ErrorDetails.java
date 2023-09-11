package com.tcmp.tiapi.shared.dto.response.error;

public record ErrorDetails(
  String field,
  String error
) {
}
