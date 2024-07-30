package com.tcmp.tiapi.program.dto.ti;

import com.tcmp.tiapi.ti.dto.MaintenanceType;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
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
@XmlRootElement(name = TIOperation.UPDATE_BASE_RATE_VALUE, namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class InterestSchedule {
    @XmlElement(name = "MaintType", namespace = TINamespace.MESSAGES)
    private MaintenanceType maintenanceType;

    @XmlElement(name = "InterestType", namespace = TINamespace.MESSAGES)
    private String interestType;

    @XmlElement(name = "Currency", namespace = TINamespace.MESSAGES)
    private String currency;

    @XmlElement(name = "SCFProgramme", namespace = TINamespace.MESSAGES)
    private BaseRateProgramme scfProgramme;

    @XmlElement(name = "Narrative", namespace = TINamespace.MESSAGES)
    private String narrative;

    @XmlElement(name = "Interpolate", namespace = TINamespace.MESSAGES)
    private Boolean interpolate;

    @XmlElement(name = "Split", namespace = TINamespace.MESSAGES)
    private Boolean split;

    @XmlElement(name = "InterestDaysBasis", namespace = TINamespace.MESSAGES)
    private String interestDaysBasis;

    @XmlElement(name = "PeriodOrAmount", namespace = TINamespace.MESSAGES)
    private String periodOrAmount;

    @XmlElement(name = "InterestRateType", namespace = TINamespace.MESSAGES)
    private String interestRateType;

    @XmlElement(name = "Tier1", namespace = TINamespace.MESSAGES)
    private Tier1 tier1;
}
