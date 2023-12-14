package com.tcmp.tiapi.invoice.dto.ti.finance;

import com.tcmp.tiapi.ti.LocalDateAdapter;
import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "InvoiceNumbers", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceNumbers {
  @XmlElement(name = "InvoiceNumber", namespace = TINamespace.MESSAGES)
  private String invoiceNumber;

  @XmlElement(name = "IssueDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate issueDate;

  @XmlElement(name = "OutstandingAmount", namespace = TINamespace.MESSAGES)
  private String outstandingAmount;

  @XmlElement(name = "OutstandingAmountCurrency", namespace = TINamespace.MESSAGES)
  private String outstandingAmountCurrency;
}
