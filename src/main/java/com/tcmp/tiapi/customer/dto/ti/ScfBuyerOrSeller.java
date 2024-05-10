package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import com.tcmp.tiapi.ti.dto.CustomerRole;
import com.tcmp.tiapi.ti.dto.TIBooleanAdapter;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_BUYER_OR_SELLER_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class ScfBuyerOrSeller {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean maintainedInBackOffice;

  @XmlElement(name = "Programme", namespace = TINamespace.MESSAGES)
  private String programme;

  @XmlElement(name = "BuyerOrSeller", namespace = TINamespace.MESSAGES)
  private String buyerOrSeller;

  @XmlElement(name = "Role", namespace = TINamespace.MESSAGES)
  private CustomerRole role;

  @XmlElement(name = "Customer", namespace = TINamespace.MESSAGES)
  private Customer customer;

  @XmlElement(name = "Name", namespace = TINamespace.MESSAGES)
  private String name;

  @XmlElement(name = "Email", namespace = TINamespace.MESSAGES)
  private String email;

  // SWIFT stuff

  @XmlElement(name = "TransferMethod", namespace = TINamespace.MESSAGES)
  private String transferMethod;

  @XmlElement(name = "Language", namespace = TINamespace.MESSAGES)
  private String language;

  @XmlElement(name = "Status", namespace = TINamespace.MESSAGES)
  private String status;

  @XmlElement(name = "InvoiceLimit", namespace = TINamespace.MESSAGES)
  private CurrencyAmount invoiceLimit;

  @XmlElement(name = "Branch", namespace = TINamespace.MESSAGES)
  private String branch;
}
