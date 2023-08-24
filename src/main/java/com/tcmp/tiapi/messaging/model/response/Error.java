package com.tcmp.tiapi.messaging.model.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Error {
  @XmlElement(name = "Error")
  private String errorText;
}
