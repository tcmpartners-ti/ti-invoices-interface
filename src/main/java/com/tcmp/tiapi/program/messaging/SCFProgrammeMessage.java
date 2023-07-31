package com.tcmp.tiapi.program.messaging;

import com.tcmp.tiapi.messaging.LocalDateAdapter;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.shared.messaging.CurrencyAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "SCFProgramme", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class SCFProgrammeMessage {
    @XmlElement(name = "ProgrammeId", namespace = TINamespace.MESSAGES)
    private String id;

    @XmlElement(name = "ProgrammeDescription", namespace = TINamespace.MESSAGES)
    private String description;

    @XmlElement(name = "Customer", namespace = TINamespace.MESSAGES)
    private ProgramCustomer customer;

    @XmlElement(name = "StartDate", namespace = TINamespace.MESSAGES)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate startDate;

    @XmlElement(name = "ExpiryDate", namespace = TINamespace.MESSAGES)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate endDate;

    @XmlElement(name = "Type", namespace = TINamespace.MESSAGES)
    private String type;

    @XmlElement(name = "CreditLimit", namespace = TINamespace.MESSAGES)
    private CurrencyAmount creditLimit;

    @XmlElement(name = "Status", namespace = TINamespace.MESSAGES)
    private String status;
}
