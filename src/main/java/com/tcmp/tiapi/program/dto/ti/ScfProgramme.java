package com.tcmp.tiapi.program.dto.ti;

import com.tcmp.tiapi.customer.dto.ti.Customer;
import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_CUSTOMER_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class ScfProgramme {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  private String maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  private String maintainedInBackOffice;

  @XmlElement(name = "ProgrammeID", namespace = TINamespace.MESSAGES)
  private String id;

  @XmlElement(name = "ProgrammeDescription", namespace = TINamespace.MESSAGES)
  private String description;

  @XmlElement(name = "Customer", namespace = TINamespace.MESSAGES)
  private Customer customer;

  @XmlElement(name = "Type", namespace = TINamespace.MESSAGES)
  private Type type;

  @XmlElement(name = "SubType", namespace = TINamespace.MESSAGES)
  private String subType;

  @XmlElement(name = "CreditLimit", namespace = TINamespace.MESSAGES)
  private CurrencyAmount creditLimit;

  @XmlElement(name = "Status", namespace = TINamespace.MESSAGES)
  private String status;

  @XmlElement(name = "StartDate", namespace = TINamespace.MESSAGES)
  private LocalDate startDate;

  @XmlElement(name = "ExpiryDate", namespace = TINamespace.MESSAGES)
  private LocalDate expiryDate;

  @XmlElement(name = "Narrative", namespace = TINamespace.MESSAGES)
  private String narrative;

  @XmlElement(name = "InvoiceUploadedBy", namespace = TINamespace.MESSAGES)
  private OperationRole invoiceUploadedBy;

  @XmlElement(name = "FinanceRequestedBy", namespace = TINamespace.MESSAGES)
  private OperationRole financeRequestedBy;

  @XmlElement(name = "FinanceDebitParty", namespace = TINamespace.MESSAGES)
  private OperationRole financeDebitParty;

  @XmlElement(name = "FinanceToParty", namespace = TINamespace.MESSAGES)
  private OperationRole financeToParty;

  @XmlElement(name = "BuyerAcceptanceRequired", namespace = TINamespace.MESSAGES)
  private String buyerAcceptanceRequired;

  @XmlElement(name = "ParentGuarantorExists", namespace = TINamespace.MESSAGES)
  private String parentGuarantorExists;

  public enum Type {
    BUYER_CENTRIC,
    SELLER_CENTRIC;

    @Override
    public String toString() {
      return switch (this) {
        case BUYER_CENTRIC -> "B";
        case SELLER_CENTRIC -> "S";
      };
    }
  }

  public enum OperationRole {
    BUYER,
    SELLER;

    @Override
    public String toString() {
      return switch (this) {
        case BUYER -> "B";
        case SELLER -> "S";
      };
    }
  }
}
