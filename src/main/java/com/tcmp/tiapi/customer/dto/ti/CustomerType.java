package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
public class CustomerType {
  @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
  private String maintenanceType;

  @XmlElement(name = "MaintainedInBackOffice", namespace = TINamespace.MESSAGES)
  private String maintainedInBackOffice;

  @XmlElement(name = "Type", namespace = TINamespace.MESSAGES)
  private String type;

  @XmlElement(name = "Description", namespace = TINamespace.MESSAGES)
  private String description;

  @XmlElement(name = "Qualifier", namespace = TINamespace.MESSAGES)
  private String qualifier;
}
