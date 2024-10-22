package com.tcmp.tiapi.program.dto.ti;

import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import com.tcmp.tiapi.ti.LocalDateAdapter;
import com.tcmp.tiapi.ti.dto.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_PROGRAMME_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class ScfProgramme {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  private String maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean maintainedInBackOffice;

  @XmlElement(name = "ProgrammeID", namespace = TINamespace.MESSAGES)
  private String id;

  @XmlElement(name = "ProgrammeDescription", namespace = TINamespace.MESSAGES)
  private String description;

  @XmlElement(name = "Customer", namespace = TINamespace.MESSAGES)
  private NestedCustomer customer;

  @XmlElement(name = "Type", namespace = TINamespace.MESSAGES)
  private Type type;

  @XmlElement(name = "SubType", namespace = TINamespace.MESSAGES)
  private String subType;

  @XmlElement(name = "CreditLimit", namespace = TINamespace.MESSAGES)
  private CurrencyAmount creditLimit;

  @XmlElement(name = "Status", namespace = TINamespace.MESSAGES)
  private String status;

  @XmlElement(name = "StartDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate startDate;

  @XmlElement(name = "ExpiryDate", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate expiryDate;

  @XmlElement(name = "Narrative", namespace = TINamespace.MESSAGES)
  private String narrative;

  @XmlElement(name = "InvoiceUploadedBy", namespace = TINamespace.MESSAGES)
  private CustomerRole invoiceUploadedBy;

  @XmlElement(name = "FinanceRequestedBy", namespace = TINamespace.MESSAGES)
  private CustomerRole financeRequestedBy;

  @XmlElement(name = "FinanceDebitParty", namespace = TINamespace.MESSAGES)
  private CustomerRole financeDebitParty;

  @XmlElement(name = "FinanceToParty", namespace = TINamespace.MESSAGES)
  private CustomerRole financeToParty;

  @XmlElement(name = "BuyerAcceptanceRequired", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean buyerAcceptanceRequired;

  @XmlElement(name = "BehalfOfBranch", namespace = TINamespace.MESSAGES)
  private String behalfOfBranch;

  @XmlElement(name = "ParentGuarantorExists", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(TIBooleanAdapter.class)
  private Boolean parentGuarantorExists;

  @XmlJavaTypeAdapter(TypeAdapter.class)
  public enum Type {
    BUYER_CENTRIC,
    SELLER_CENTRIC
  }

  private static class TypeAdapter extends XmlAdapter<String, Type> {
    @Override
    public Type unmarshal(String s) {
      return switch (s) {
        case "B" -> Type.BUYER_CENTRIC;
        case "S" -> Type.SELLER_CENTRIC;
        default -> null;
      };
    }

    @Override
    public String marshal(Type t) {
      return switch (t) {
        case BUYER_CENTRIC -> "B";
        case SELLER_CENTRIC -> "S";
      };
    }
  }
}
