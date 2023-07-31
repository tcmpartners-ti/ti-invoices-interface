package com.tcmp.tiapi.shared.messaging;

import com.tcmp.tiapi.messaging.model.TINamespace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CurrencyAmount implements Serializable {
    @XmlElement(name = "Amount", namespace = TINamespace.COMMON)
    private BigDecimal amount;

    @XmlElement(name = "Currency", namespace = TINamespace.COMMON)
    private String currency;
}
