package com.tcmp.tiapi.invoice.dto.ti;

import com.tcmp.tiapi.messaging.model.TINamespace;
import jakarta.validation.constraints.Size;
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
  @Size(min = 1, max = 20, message = "Customer must be between 1 and 20 characters.")
  @XmlElement(name = "Customer", namespace = TINamespace.COMMON)
  private String customer;

  @Size(min = 1, max = 34, message = "Their reference must be between 1 and 34 characters.")
  @XmlElement(name = "TheirReference", namespace = TINamespace.COMMON)
  private String theirReference;

  @Size(min = 1, max = 8, message = "Behalf of Branch must be between 1 and 8 characters.")
  @XmlElement(name = "BehalfOfBranch", namespace = TINamespace.COMMON)
  private String behalfOfBranch;
}
