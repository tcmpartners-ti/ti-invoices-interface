package com.tcmp.tiapi.ti.dto.response;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "ResponseHeader", namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseHeader {
  @XmlElement(name = "Service", namespace = TINamespace.CONTROL)
  private String service;

  @XmlElement(name = "Operation", namespace = TINamespace.CONTROL)
  private String operation;

  @XmlElement(name = "Status", namespace = TINamespace.CONTROL)
  private String status;

  @XmlElement(name = "Details", namespace = TINamespace.CONTROL)
  private Details details;

  @XmlElement(name = "CorrelationId", namespace = TINamespace.CONTROL)
  private String correlationId;
}
