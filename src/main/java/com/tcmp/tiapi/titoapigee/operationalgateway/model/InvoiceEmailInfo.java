package com.tcmp.tiapi.titoapigee.operationalgateway.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InvoiceEmailInfo(
  String customerMnemonic,
  String customerEmail,
  String customerName,
  String date,
  String action,
  String invoiceNumber,
  String invoiceCurrency,
  BigDecimal amount
) {
}
