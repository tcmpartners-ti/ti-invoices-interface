package com.tcmp.tiapi.messaging.model.requests;

import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
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
@XmlRootElement(name = "ServiceRequest",namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
  CreateDueInvoiceEventMessage.class
})

public class AckServiceRequest<T> {

  @XmlElement(name = "RequestHeader",namespace = TINamespace.CONTROL)
  private AckRequestHeader header;

  @XmlElement(
      name = TIOperation.DUE_INVOICE_VALUE,
      type = CreateDueInvoiceEventMessage.class,
      namespace = TINamespace.CONTROL
  )

  private T body;

}
