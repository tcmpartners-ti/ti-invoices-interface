package com.tcmp.tiapi.invoice.dto.ti.financeack;

import com.tcmp.tiapi.messaging.model.TINamespace;
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
@XmlRootElement(name = "FinancePaymentDetails", namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
public class FinancePaymentDetails {
  @XmlElement(name = "FinancePaymentDetails", namespace = TINamespace.CONTROL)
  private String details;

  @XmlElement(name = "Amount", namespace = TINamespace.CONTROL)
  private String amount;

  @XmlElement(name = "Currency", namespace = TINamespace.CONTROL)
  private String currency;

  @XmlElement(name = "ValueDate", namespace = TINamespace.CONTROL)
  private String valueDate;

  @XmlElement(name = "AccountDetails", namespace = TINamespace.CONTROL)
  private String accountDetails;

  @XmlElement(name = "SettlementParty", namespace = TINamespace.CONTROL)
  private String settlementParty;
}
