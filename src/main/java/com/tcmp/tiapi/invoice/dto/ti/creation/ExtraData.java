package com.tcmp.tiapi.invoice.dto.ti.creation;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtraData implements Serializable {
  @XmlElement(name = "FinanceAccount", namespace = TINamespace.CUSTOM)
  private String financeAccount;

  @XmlElement(name = "GafOperationId", namespace = TINamespace.CUSTOM)
  private String gafOperationId;

  @XmlElement(name = "GafInterestRate", namespace = TINamespace.CUSTOM)
  private BigDecimal gafInterestRate;

  @XmlElement(name = "GafDisbursementAmount", namespace = TINamespace.CUSTOM)
  private BigDecimal gafDisbursementAmount;

  @XmlElement(name = "GafTaxFactor", namespace = TINamespace.CUSTOM)
  private BigDecimal gafTaxFactor;

  @XmlElement(name = "BuyerGafInterests", namespace = TINamespace.CUSTOM)
  private BigDecimal buyerGafInterests;

  @XmlElement(name = "SellerGafInterests", namespace = TINamespace.CUSTOM)
  private BigDecimal sellerGafInterests;

  @XmlElement(name = "BuyerSolcaAmount", namespace = TINamespace.CUSTOM)
  private BigDecimal buyerSolcaAmount;

  @XmlElement(name = "SellerSolcaAmount", namespace = TINamespace.CUSTOM)
  private BigDecimal sellerSolcaAmount;

  @XmlElement(name = "GafAmortizations", namespace = TINamespace.CUSTOM)
  private String gafAmortizations;
}
