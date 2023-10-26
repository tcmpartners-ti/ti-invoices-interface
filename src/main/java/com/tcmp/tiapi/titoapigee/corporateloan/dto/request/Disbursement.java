package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import lombok.Builder;

@Builder
public record Disbursement(
  String accountNumber,
  String accountType,
  String bankId,
  String form
) {
}
