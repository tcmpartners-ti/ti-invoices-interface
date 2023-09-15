package com.tcmp.tiapi.invoice.dto.ti;

import com.tcmp.tiapi.messaging.LocalDateAdapter;
import com.tcmp.tiapi.messaging.model.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
