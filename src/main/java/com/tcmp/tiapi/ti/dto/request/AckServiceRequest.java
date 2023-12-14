package com.tcmp.tiapi.ti.dto.request;

import com.tcmp.tiapi.invoice.dto.ti.cancel.CancelInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.creation.InvoiceCreationResultMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperationTag;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "ServiceRequest", namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
  InvoiceSettlementEventMessage.class,
  FinanceAckMessage.class,
  InvoiceCreationResultMessage.class,
  CancelInvoiceEventMessage.class,
})
public class AckServiceRequest<T> {

  @XmlElement(name = "RequestHeader", namespace = TINamespace.CONTROL)
  private AckRequestHeader header;

  @XmlElements({
    @XmlElement(
        name = TIOperationTag.INVOICE_SETTLEMENT,
        type = InvoiceSettlementEventMessage.class,
        namespace = TINamespace.CONTROL),
    @XmlElement(
        name = TIOperationTag.INVOICE_FINANCING_RESULT,
        type = FinanceAckMessage.class,
        namespace = TINamespace.CONTROL),
    @XmlElement(
        name = TIOperationTag.INVOICE_CREATION_RESULT,
        type = InvoiceCreationResultMessage.class,
        namespace = TINamespace.CONTROL),
    @XmlElement(
        name = TIOperationTag.INVOICE_CANCELLATION_RESULT,
        type = CancelInvoiceEventMessage.class,
        namespace = TINamespace.CONTROL),
  })
  private T body;
}
