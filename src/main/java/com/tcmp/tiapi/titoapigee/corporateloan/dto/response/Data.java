package com.tcmp.tiapi.titoapigee.corporateloan.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record Data(
  String operationId,
  int interestRate,
  double effectiveRate,
  double disbursementAmount,
  Tax tax,
  double totalInstallmentsAmount,
  List<Amortization> amortizations,
  Error error
) {
}
