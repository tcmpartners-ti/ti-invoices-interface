package com.tcmp.tiapi.program.messaging;

import com.tcmp.tiapi.messaging.model.TINamespace;
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
@XmlRootElement(name = "Customer", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class ProgramCustomer {
  @XmlElement(name = "Mnemonic", namespace = TINamespace.COMMON)
  private String mnemonic;
}
