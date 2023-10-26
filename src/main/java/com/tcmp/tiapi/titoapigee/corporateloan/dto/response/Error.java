package com.tcmp.tiapi.titoapigee.corporateloan.dto.response;

public record Error(
  String code,
  String message,
  String type
) {
}
