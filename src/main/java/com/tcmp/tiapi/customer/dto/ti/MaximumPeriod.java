package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.ti.dto.TINamespace;
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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MaximumPeriod {
  @XmlElement(name = "TenorDays", namespace = TINamespace.COMMON)
  private Integer days;

  @XmlElement(name = "TenorPeriod", namespace = TINamespace.COMMON)
  private String period;
}
