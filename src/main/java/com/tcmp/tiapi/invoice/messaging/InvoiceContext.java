package com.tcmp.tiapi.invoice.messaging;

import com.tcmp.tiapi.messaging.model.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceContext implements Serializable {
  @XmlElement(name = "Branch", namespace = TINamespace.COMMON)
  private String branch;

  @XmlElement(name = "Customer", namespace = TINamespace.COMMON)
  private String customer;

  @XmlElement(name = "TheirReference", namespace = TINamespace.COMMON)
  private String theirReference;

  @XmlElement(name = "BehalfOfBranch", namespace = TINamespace.COMMON)
  private String behalfOfBranch;
}
