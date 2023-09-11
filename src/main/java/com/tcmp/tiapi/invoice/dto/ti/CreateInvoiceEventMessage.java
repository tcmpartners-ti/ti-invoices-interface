package com.tcmp.tiapi.invoice.dto.ti;

import com.tcmp.tiapi.messaging.LocalDateAdapter;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Valid
  @XmlElement(name = "Context", namespace = TINamespace.MESSAGES)
  private InvoiceContext context;

  @NotNull(message = "Programme identifier is required.")
  @Size(max = 35, message = "Program identifier max length is 35 characters.")
  @XmlElement(name = "Programme", namespace = TINamespace.MESSAGES)
  private String programme;

  @Size(min = 1, max = 20, message = "Seller mnemonic must be between 1 and 20 characters.")
  @XmlElement(name = "Seller", namespace = TINamespace.MESSAGES)
  private String seller;

  @Size(min = 1, max = 20, message = "Buyer mnemonic must be between 1 and 20 characters.")
  @XmlElement(name = "Buyer", namespace = TINamespace.MESSAGES)
  private String buyer;

  @XmlElement(name = "AnchorParty", namespace = TINamespace.MESSAGES)
  private String anchorParty;

  @XmlElement(name = "BatchID", namespace = TINamespace.MESSAGES)
  private String batchId;

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

  @XmlElement(name = "InvoiceApproved", namespace = TINamespace.MESSAGES)
  private String invoiceApproved;
}
