package com.tcmp.tiapi.invoice.dto.ti.financeack;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "InvoiceArray")
@XmlAccessorType(XmlAccessType.FIELD)
public class Invoice {
  @XmlElement(name = "InvoiceReference")
  private String invoiceReference;

  @XmlElement(name = "InvoiceNumber")
  private String invoiceNumber;

  @XmlElement(name = "InvoiceIssueDate")
  private String invoiceIssueDate;

  @XmlElement(name = "InvoiceSettlementDate")
  private String invoiceSettlementDate;

  @XmlElement(name = "InvoiceOutstandingAmount")
  private String invoiceOutstandingAmount;

  @XmlElement(name = "InvoiceOutstandingAmountCurrency")
  private String invoiceOutstandingAmountCurrency;

  @XmlElement(name = "InvoiceAdvanceAmount")
  private String invoiceAdvanceAmount;

  @XmlElement(name = "InvoiceAdvanceAmountCurrency")
  private String invoiceAdvanceAmountCurrency;
}
