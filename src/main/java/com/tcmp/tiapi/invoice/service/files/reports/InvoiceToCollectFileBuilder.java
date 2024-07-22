package com.tcmp.tiapi.invoice.service.files.reports;

import com.tcmp.tiapi.invoice.model.InvoiceToCollectReport;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import org.springframework.stereotype.Component;

@Component
public class InvoiceToCollectFileBuilder {
  public String[] header() {
    return new String[] {
      "BuyerMnemonic",
      "SellerMnemonic",
      "InvoiceReference",
      "InvoiceDueDate",
      "InvoiceFaceAmount",
      "InvoiceStatus",
      "BuyerName",
      "SellerName",
      "ProgrammeId",
      "FinanceAmount",
      "SellerInterests",
      "BuyerInterestsRate",
      "SellerSolcaAmount",
      "InvoiceDateReceived",
      "FinanceEffectiveDate",
      "FinanceEventId"
    };
  }

  public String[] buildRow(InvoiceToCollectReport report) {
    return new String[] {
      StringMappingUtils.trimNullable(report.getBuyerMnemonic()),
      StringMappingUtils.trimNullable(report.getSellerMnemonic()),
      StringMappingUtils.trimNullable(report.getInvoiceReference()),
      StringMappingUtils.toStringNullable(report.getInvoiceDueDate()),
      StringMappingUtils.toStringNullable(report.getInvoiceFaceAmount()),
      StringMappingUtils.trimNullable(report.getInvoiceStatus()),
      StringMappingUtils.trimNullable(report.getBuyerName()),
      StringMappingUtils.trimNullable(report.getSellerName()),
      StringMappingUtils.trimNullable(report.getProgrammeId()),
      StringMappingUtils.toStringNullable(report.getFinanceAmount()),
      StringMappingUtils.toStringNullable(report.getSellerInterests()),
      StringMappingUtils.toStringNullable(report.getBuyerInterestsRate()),
      StringMappingUtils.toStringNullable(report.getSellerSolcaAmount()),
      StringMappingUtils.toStringNullable(report.getInvoiceDateReceived()),
      StringMappingUtils.toStringNullable(report.getFinanceEffectiveDate()),
      StringMappingUtils.trimNullable(report.getFinanceEventId())
    };
  }
}
