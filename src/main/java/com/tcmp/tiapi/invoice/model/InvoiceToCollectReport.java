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

  String getProgrammeType();

  String getProgrammeSubtype();

  Integer getProgrammeExtraFinancingDays();

  BigDecimal getFinanceAmount();

  BigDecimal getSellerInterests();

  BigDecimal getBuyerInterestsRate();

  BigDecimal getSellerSolcaAmount();

  String getGafOperationId();

  LocalDate getInvoiceDateReceived();

  LocalDate getFinanceEffectiveDate();

  String getFinanceEventId();
}
