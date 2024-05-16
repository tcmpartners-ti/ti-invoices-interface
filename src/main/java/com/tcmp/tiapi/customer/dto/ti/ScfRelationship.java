package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import com.tcmp.tiapi.ti.dto.TIBooleanAdapter;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(
    name = TIOperation.CREATE_BUYER_SELLER_RELATIONSHIP_VALUE,
    namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class ScfRelationship {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  private String maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean maintainedInBackOffice;

  @XmlElement(name = "MakerCheckerRequired", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean makerCheckerRequired;

  @XmlElement(name = "Programme", namespace = TINamespace.MESSAGES)
  private String programme;

  @XmlElement(name = "Seller", namespace = TINamespace.MESSAGES)
  private String seller;

  @XmlElement(name = "Buyer", namespace = TINamespace.MESSAGES)
  private String buyer;

  @XmlElement(name = "CustomerBuyerLimit", namespace = TINamespace.MESSAGES)
  private CurrencyAmount customerBuyerLimit;

  @XmlElement(name = "BuyerPercent", namespace = TINamespace.MESSAGES)
  private BigDecimal buyerPercent;

  @XmlElement(name = "MaximumPeriod", namespace = TINamespace.MESSAGES)
  private MaximumPeriod maximumPeriod;

  // Security Details

  @XmlElement(name = "Recourse", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean recourse;

  @XmlElement(name = "Disclosed", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean disclosed;

  @XmlElement(name = "CalculateEligibilityFromDateOfReceipt", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean calculateEligibilityFromDateOfReceipt;
}
