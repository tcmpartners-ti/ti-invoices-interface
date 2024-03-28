package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.ti.LocalDateAdapter;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_ACCOUNT_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class Account {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  private String maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  private String maintainedInBackOffice;

  @XmlElement(name = "BackOfficeAccount", namespace = TINamespace.MESSAGES)
  private String backOfficeAccount;

  @XmlElement(name = "Branch", namespace = TINamespace.MESSAGES)
  private String branch;

  @XmlElement(name = "Customer", namespace = TINamespace.MESSAGES)
  private Account.AccountCustomer customer;

  @XmlElement(name = "AccountType", namespace = TINamespace.MESSAGES)
  private String type;

  @XmlElement(name = "Currency", namespace = TINamespace.MESSAGES)
  private String currency;

  @XmlElement(name = "ExternalAccount", namespace = TINamespace.MESSAGES)
  private String externalAccount;

  @XmlElement(name = "ShortName", namespace = TINamespace.MESSAGES)
  private String shortName;

  @XmlElement(name = "DateOpened", namespace = TINamespace.MESSAGES)
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate dateOpened;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @XmlRootElement(name = TIOperation.CREATE_CUSTOMER_VALUE, namespace = TINamespace.MESSAGES)
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AccountCustomer {
    @XmlElement(name = "SourceBankingBusiness", namespace = TINamespace.COMMON)
    private String sourceBankingBusiness;

    @XmlElement(name = "Mnemonic", namespace = TINamespace.COMMON)
    private String mnemonic;
  }

  @SuppressWarnings("java:S107")
  @Builder
  public Account(
      String maintenanceType,
      String maintainedInBackOffice,
      String branch,
      AccountCustomer customer,
      String type,
      String currency,
      String externalAccount,
      String shortName,
      LocalDate dateOpened) {
    this.maintenanceType = maintenanceType;
    this.maintainedInBackOffice = maintainedInBackOffice;
    this.branch = branch;
    this.customer = customer;
    this.type = type;
    this.currency = currency;
    this.externalAccount = externalAccount;
    this.shortName = shortName;
    this.dateOpened = dateOpened;
    this.backOfficeAccount =
        String.format("BO-%s-%s-%s-%s-1", branch, customer.getMnemonic(), type, currency);
  }
}
