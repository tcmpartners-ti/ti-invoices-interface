package com.tcmp.tiapi.invoice.dto.ti.finance;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "TFBUYFIN", namespace = TINamespace.MESSAGES)
public class FinanceBuyerCentricInvoiceEventMessage extends FinanceInvoiceEventMessage {

    @XmlElement(name = "ExtraData", namespace = TINamespace.MESSAGES)
    private ExtraFinancingData extraFinancingData;
}
