package com.tcmp.tiapi.invoice.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InvoiceToCollectReport {
  String getBuyerMnemonic();

  String getSellerMnemonic();

  String getInvoiceReference();

  LocalDate getInvoiceDueDate();

  BigDecimal getInvoiceFaceAmount();

  String getInvoiceStatus();

  String getBuyerName();

  String getSellerName();

  String getProgrammeId();

  BigDecimal getFinanceAmount();

  BigDecimal getSellerInterests();

  BigDecimal getBuyerInterestsRate();

  BigDecimal getSellerSolcaAmount();

  LocalDate getInvoiceDateReceived();

  LocalDate getFinanceEffectiveDate();

  String getFinanceEventId();
}
