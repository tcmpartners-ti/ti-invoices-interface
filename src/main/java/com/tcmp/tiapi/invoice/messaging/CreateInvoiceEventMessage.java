package com.tcmp.tiapi.invoice.messaging;

import com.tcmp.tiapi.messaging.LocalDateAdapter;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "TFINVNEW", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateInvoiceEventMessage implements Serializable {
  @XmlElement(name = "Context", namespace = TINamespace.MESSAGES)
  private InvoiceContext context;

  @XmlElement(name = "AnchorParty", namespace = TINamespace.MESSAGES)
  private String anchorParty;

  @XmlElement(name = "Programme", namespace = TINamespace.MESSAGES)
  private String programme;

  @XmlElement(name = "Seller", namespace = TINamespace.MESSAGES)
  private String seller;

  @XmlElement(name = "Buyer", namespace = TINamespace.MESSAGES)
  private String buyer;

  @XmlElement(name = "ReceivedOn", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate receivedOn;

  @XmlElement(name = "InvoiceNumber", namespace = TINamespace.MESSAGES)
  private String invoiceNumber;

  @XmlElement(name = "IssueDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate issueDate;

  @XmlElement(name = "FaceValue", namespace = TINamespace.MESSAGES)
  private CurrencyAmount faceValue;

  @XmlElement(name = "OutstandingAmount", namespace = TINamespace.MESSAGES)
  private CurrencyAmount outstandingAmount;

  @XmlElement(name = "SettlementDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate settlementDate;
}
