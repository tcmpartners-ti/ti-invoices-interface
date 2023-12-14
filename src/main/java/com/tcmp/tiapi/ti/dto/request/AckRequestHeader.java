package com.tcmp.tiapi.ti.dto.request;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "RequestHeader")
@XmlAccessorType(XmlAccessType.FIELD)
public class AckRequestHeader {
  @XmlElement(name = "Service", namespace = TINamespace.CONTROL)
  private String service;

  @XmlElement(name = "Operation", namespace = TINamespace.CONTROL)
  private String operation;

  @XmlElement(name = "Credentials", namespace = TINamespace.CONTROL)
  private Credentials credentials;

  @XmlElement(name = "ReplyFormat", namespace = TINamespace.CONTROL)
  private String replyFormat;

  @XmlElement(name = "TargetSystem", namespace = TINamespace.CONTROL)
  private String targetSystem;

  @XmlElement(name = "SourceSystem", namespace = TINamespace.CONTROL)
  private String sourceSystem;

  @XmlElement(name = "NoRepair", namespace = TINamespace.CONTROL)
  private String noRepair;

  @XmlElement(name = "NoOverride", namespace = TINamespace.CONTROL)
  private String noOverride;

  @XmlElement(name = "CorrelationId", namespace = TINamespace.CONTROL)
  private String correlationId;

  @XmlElement(name = "TransactionControl", namespace = TINamespace.CONTROL)
  private String transactionControl;

  @XmlElement(name = "CreationDate", namespace = TINamespace.CONTROL)
  private String creationDate;
}
