package com.tcmp.tiapi.invoice.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InvoiceToPayReport {
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

  BigDecimal getFinanceAmount();

  LocalDate getFinanceEffectiveDate();

  BigDecimal getBuyerInterests();

  BigDecimal getBuyerInterestsRate();

  BigDecimal getBuyerSolcaAmount();

  LocalDate getInvoiceDateReceived();

  Integer getProgramExtraFinancingDays();

  String getGafOperationId();
}
