package com.tcmp.tiapi.invoice.dto.ti.creation;

import com.tcmp.tiapi.invoice.dto.ti.ExtraData;
import com.tcmp.tiapi.invoice.dto.ti.InvoiceContext;
import com.tcmp.tiapi.ti.LocalDateAdapter;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_INVOICE_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateInvoiceEventMessage implements Serializable {
  @Serial private static final long serialVersionUID = 8917249871L;

  @XmlElement(name = "Context", namespace = TINamespace.MESSAGES)
  private InvoiceContext context;

  @XmlElement(name = "Programme", namespace = TINamespace.MESSAGES)
  private String programme;

  @XmlElement(name = "Seller", namespace = TINamespace.MESSAGES)
  private String seller;

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

  // For invoice creation: finance account = anchor finance account
  @XmlElement(name = "ExtraData", namespace = TINamespace.MESSAGES)
  private ExtraData extraData;
}
