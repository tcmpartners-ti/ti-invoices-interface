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
@XmlRootElement(name = "FinancePaymentDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class FinancePaymentDetails {
  @XmlElement(name = "FinancePaymentDetails")
  private String details;

  @XmlElement(name = "Amount")
  private String amount;

  @XmlElement(name = "Currency")
  private String currency;

  @XmlElement(name = "ValueDate")
  private String valueDate;

  @XmlElement(name = "AccountDetails")
  private String accountDetails;

  @XmlElement(name = "SettlementParty")
  private String settlementParty;
}
