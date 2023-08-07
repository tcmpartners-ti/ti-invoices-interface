package com.tcmp.tiapi.messaging.model.requests;

import jakarta.xml.bind.annotation.*;
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
