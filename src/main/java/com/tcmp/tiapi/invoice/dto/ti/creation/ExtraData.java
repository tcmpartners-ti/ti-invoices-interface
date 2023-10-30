package com.tcmp.tiapi.invoice.dto.ti.creation;

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
public class ExtraData implements Serializable {
  @XmlElement(name = "FinanceAccount", namespace = TINamespace.CUSTOM)
  private String financeAccount;
}
