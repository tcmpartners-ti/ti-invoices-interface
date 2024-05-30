package com.tcmp.tiapi.invoice.dto.ti;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
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
public class InvoiceContext implements Serializable {
  @XmlElement(name = "Branch", namespace = TINamespace.COMMON)
  private String branch;

  @XmlElement(name = "Customer", namespace = TINamespace.COMMON)
  private String customer;

  @XmlElement(name = "OurReference", namespace = TINamespace.COMMON)
  private String ourReference;

  @XmlElement(name = "TheirReference", namespace = TINamespace.COMMON)
  private String theirReference;

  @XmlElement(name = "BehalfOfBranch", namespace = TINamespace.COMMON)
  private String behalfOfBranch;
}
