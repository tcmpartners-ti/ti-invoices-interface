package com.tcmp.tiapi.titoapigee.corporateloan.dto.response;

import java.util.List;

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