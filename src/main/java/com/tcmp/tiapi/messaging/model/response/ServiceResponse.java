package com.tcmp.tiapi.messaging.model.response;

import com.tcmp.tiapi.messaging.model.TINamespace;
import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "ServiceResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ServiceResponse {
  @XmlAttribute(namespace = TINamespace.CONTROL)
  private String controlNamespace;

  @XmlAttribute(namespace = TINamespace.CONTROL)
  private String messagesNamespace;

  @XmlAttribute(namespace = TINamespace.COMMON)
  private String commonNamespace;

  @XmlElement(name = "ResponseHeader")
  private ResponseHeader responseHeader;
}
