package com.tcmp.tiapi.titoapigee.operationalgateway.model;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record InvoiceEmailInfo(
    String customerMnemonic,
    String customerEmail,
    String customerName,
    String date,
    String action,
    String invoiceNumber,
    String invoiceCurrency,
    BigDecimal amount) {}
