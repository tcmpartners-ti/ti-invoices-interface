package com.tcmp.tiapi.messaging.model.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
