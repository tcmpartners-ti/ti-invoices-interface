package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.*;
import java.util.List;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_CUSTOMER_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  private String maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  private String maintainedInBackOffice;

  @XmlElement(name = "SourceBankingBusiness", namespace = TINamespace.MESSAGES)
  private String sourceBankingBusiness;

  @XmlElement(name = "Mnemonic", namespace = TINamespace.MESSAGES)
  private String mnemonic;

  @XmlElement(name = "CustomerNumber", namespace = TINamespace.MESSAGES)
  private String number;

  @XmlElement(name = "CustomerType", namespace = TINamespace.MESSAGES)
  private String type;

  @XmlElement(name = "FullName", namespace = TINamespace.MESSAGES)
  private String fullName;

  @XmlElement(name = "ShortName", namespace = TINamespace.MESSAGES)
  private String shortName;

  @XmlElement(name = "BankCode1", namespace = TINamespace.MESSAGES)
  private String bankCode;

  @XmlElementWrapper(name = "AddressDetails", namespace = TINamespace.MESSAGES)
  @XmlElement(name = "AddressDetail", namespace = TINamespace.MESSAGES)
  private List<AddressDetail> addressDetails;

  @XmlElement(name = "OtherDetails", namespace = TINamespace.MESSAGES)
  @Getter
  private OtherDetails otherDetails;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @XmlRootElement(name = "AddressDetail", namespace = TINamespace.MESSAGES)
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class AddressDetail {
    private static final String DEFAULT_TRANSFER_METHOD = "GW"; // Gateway
    private static final String DEFAULT_LOCALE = "EN_GB";

    @XmlElement(name = "AddressType", namespace = TINamespace.MESSAGES)
    private String addressType;

    @XmlElement(name = "NameAndAddress", namespace = TINamespace.MESSAGES)
    private String nameAndAddress;

    @XmlElement(name = "Locale", namespace = TINamespace.MESSAGES)
    @Builder.Default
    private String locale = DEFAULT_LOCALE;

    @XmlElement(name = "Phone", namespace = TINamespace.MESSAGES)
    private String phone;

    @XmlElement(name = "Email", namespace = TINamespace.MESSAGES)
    private String email;

    @XmlElement(name = "TransferMethod", namespace = TINamespace.MESSAGES)
    @Builder.Default
    private String transferMethod = DEFAULT_TRANSFER_METHOD;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @XmlRootElement(name = "AddressDetail", namespace = TINamespace.MESSAGES)
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class OtherDetails {
    @XmlElement(name = "CorporateAccess", namespace = TINamespace.COMMON)
    private String corporateAccess;
  }
}
