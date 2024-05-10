package com.tcmp.tiapi.program.dto.ti;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FinanceProductType {
  @XmlElement(name = "Product", namespace = TINamespace.COMMON)
  private String product;

  @XmlElement(name = "ProductType", namespace = TINamespace.COMMON)
  private String type;

  @XmlElement(name = "ParameterSet", namespace = TINamespace.COMMON)
  private String parameterSet;
}
