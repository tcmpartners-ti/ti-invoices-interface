package com.tcmp.tiapi.messaging.model.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;


@XmlRootElement(name = "ResponseHeader")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ResponseHeader {
  @XmlElement(name = "Service")
  private String service;

  @XmlElement(name = "Operation")
  private String operation;

  @XmlElement(name = "Status")
  private String status;

  @XmlElement(name = "Details")
  private Details details;

  @XmlElement(name = "CorrelationId")
  private String correlationId;
}
