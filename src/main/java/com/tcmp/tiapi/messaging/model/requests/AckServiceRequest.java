package com.tcmp.tiapi.messaging.model.requests;

import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.NotificationInvoiceCreationMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
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
  CreateDueInvoiceEventMessage.class,
  FinanceAckMessage.class,
  NotificationInvoiceCreationMessage.class
})
public class AckServiceRequest<T> {

  @XmlElement(name = "RequestHeader", namespace = TINamespace.CONTROL)
  private AckRequestHeader header;

  @XmlElements({
    @XmlElement(
        name = TIOperation.DUE_INVOICE_DETAILS_VALUE,
        type = CreateDueInvoiceEventMessage.class,
        namespace = TINamespace.CONTROL),
    @XmlElement(
        name = TIOperation.FINANCE_ACK_INVOICE_DETAILS_VALUE,
        type = FinanceAckMessage.class,
        namespace = TINamespace.CONTROL),
    @XmlElement(
        name = TIOperation.NOTIFICATION_CREATION_ACK_INVOICE_DETAILS_VALUE,
        type = NotificationInvoiceCreationMessage.class,
        namespace = TINamespace.CONTROL),
  })
  private T body;
}
