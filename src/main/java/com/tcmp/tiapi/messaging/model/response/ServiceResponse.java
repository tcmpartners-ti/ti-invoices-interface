package com.tcmp.tiapi.messaging.model.response;

import com.tcmp.tiapi.messaging.model.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "ServiceResponse", namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ServiceResponse {
  @XmlElement(name = "ResponseHeader", namespace = TINamespace.CONTROL)
  private ResponseHeader responseHeader;
}
