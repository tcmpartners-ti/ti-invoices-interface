package com.tcmp.tiapi.shared.messaging;

import com.tcmp.tiapi.messaging.model.TINamespace;
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
public class CurrencyAmount implements Serializable {
  // Use string due to specific format required by TI (#,##0.0)
  @XmlElement(name = "Amount", namespace = TINamespace.COMMON)
  private String amount;

  @XmlElement(name = "Currency", namespace = TINamespace.COMMON)
  private String currency;
}
