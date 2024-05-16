package com.tcmp.tiapi.ti.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is used in messages that have different inner namespaces (in this case common), such
 * as: {@link com.tcmp.tiapi.program.dto.ti.ScfProgramme} classes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperation.CREATE_CUSTOMER_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class NestedCustomer {
  @XmlElement(name = "SourceBankingBusiness", namespace = TINamespace.COMMON)
  private String sourceBankingBusiness;

  @XmlElement(name = "Mnemonic", namespace = TINamespace.COMMON)
  private String mnemonic;
}
