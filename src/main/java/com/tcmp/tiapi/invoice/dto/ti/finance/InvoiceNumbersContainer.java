package com.tcmp.tiapi.invoice.dto.ti.finance;

import com.tcmp.tiapi.ti.model.TINamespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "InvoiceNumbers", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceNumbersContainer {
  @XmlElement(name = "InvoiceNumbers", namespace = TINamespace.MESSAGES)
  private List<InvoiceNumbers> invoiceNumbers;
}
