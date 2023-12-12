package com.tcmp.tiapi.invoice.dto.ti.financeack;

import com.tcmp.tiapi.ti.model.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "InvoiceArray", namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
public class Invoice {
  @XmlElement(name = "InvoiceReference", namespace = TINamespace.CONTROL)
  private String invoiceReference;

  @XmlElement(name = "InvoiceNumber", namespace = TINamespace.CONTROL)
  private String invoiceNumber;

  @XmlElement(name = "InvoiceIssueDate", namespace = TINamespace.CONTROL)
  private String invoiceIssueDate;

  @XmlElement(name = "InvoiceSettlementDate", namespace = TINamespace.CONTROL)
  private String invoiceSettlementDate;

  @XmlElement(name = "InvoiceOutstandingAmount", namespace = TINamespace.CONTROL)
  private String invoiceOutstandingAmount;

  @XmlElement(name = "InvoiceOutstandingAmountCurrency", namespace = TINamespace.CONTROL)
  private String invoiceOutstandingAmountCurrency;

  @XmlElement(name = "InvoiceAdvanceAmount", namespace = TINamespace.CONTROL)
  private String invoiceAdvanceAmount;

  @XmlElement(name = "InvoiceAdvanceAmountCurrency", namespace = TINamespace.CONTROL)
  private String invoiceAdvanceAmountCurrency;
}
