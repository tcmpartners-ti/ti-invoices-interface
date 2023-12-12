package com.tcmp.tiapi.invoice.dto.ti.finance;

import com.tcmp.tiapi.invoice.dto.ti.InvoiceContext;
import com.tcmp.tiapi.ti.LocalDateAdapter;
import com.tcmp.tiapi.ti.model.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "TFBUYFIN", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class FinanceBuyerCentricInvoiceEventMessage {
  @XmlElement(name = "Context", namespace = TINamespace.MESSAGES)
  private InvoiceContext context;

  @XmlElement(name = "TheirRef", namespace = TINamespace.MESSAGES)
  private String theirReference;

  @XmlElement(name = "Programme", namespace = TINamespace.MESSAGES)
  private String programme;

  @XmlElement(name = "Seller", namespace = TINamespace.MESSAGES)
  private String seller;

  @XmlElement(name = "Buyer", namespace = TINamespace.MESSAGES)
  private String buyer;

  @XmlElement(name = "AnchorParty", namespace = TINamespace.MESSAGES)
  private String anchorParty;

  @XmlElement(name = "MaturityDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate maturityDate;

  @XmlElement(name = "FinanceCurrency", namespace = TINamespace.MESSAGES)
  private String financeCurrency;

  @XmlElement(name = "FinancePercent", namespace = TINamespace.MESSAGES)
  private BigDecimal financePercent;

  @XmlElement(name = "FinanceDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate financeDate;

  @XmlElement(name = "InvoiceNumberss", namespace = TINamespace.MESSAGES)
  private InvoiceNumbersContainer invoiceNumbersContainer;
}
