package com.tcmp.tiapi.invoice.service.files.reports;

import com.tcmp.tiapi.invoice.model.InvoiceToPayReport;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import org.springframework.stereotype.Component;

@Component
public class InvoiceToPayFileBuilder {
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
      "BuyerInterests",
      "BuyerInterestsRate",
      "BuyerSolcaAmount",
      "InvoiceDateReceived",
      "ProgramExtraFinancingDays",
      "GafOperationId"
    };
  }

  public String[] buildRow(InvoiceToPayReport report) {
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
      StringMappingUtils.toStringNullable(report.getBuyerInterests()),
      StringMappingUtils.toStringNullable(report.getBuyerInterestsRate()),
      StringMappingUtils.toStringNullable(report.getBuyerSolcaAmount()),
      StringMappingUtils.toStringNullable(report.getInvoiceDateReceived()),
      StringMappingUtils.toStringNullable(report.getProgramExtraFinancingDays()),
      StringMappingUtils.trimNullable(report.getGafOperationId())
    };
  }
}
