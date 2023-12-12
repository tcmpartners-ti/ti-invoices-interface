package com.tcmp.tiapi.ti.model.requests;

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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Credentials {
  @XmlElement(name = "Name")
  private String name;

  @XmlElement(name = "Password")
  private String password;

  @XmlElement(name = "Certificate")
  private String certificate;

  @XmlElement(name = "Digest")
  private String digest;
}
