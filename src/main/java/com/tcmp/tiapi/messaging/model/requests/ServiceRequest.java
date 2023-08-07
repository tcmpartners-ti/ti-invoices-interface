package com.tcmp.tiapi.messaging.model.requests;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "ServiceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
  CreateInvoiceEventMessage.class,
  SCFProgrammeMessage.class
})
public class ServiceRequest<T> {
  // Missing namespaces
  @XmlAttribute(namespace = TINamespace.SCHEMA_NAMESPACE)
  private String schemaNamespace;
  @XmlAttribute(name = "xmlns", namespace = TINamespace.CONTROL)
  private String controlNamespace;

  @XmlElement(name = "RequestHeader")
  private RequestHeader header;

  @XmlElements({
    @XmlElement(
      name = TIOperation.CREATE_INVOICE_VALUE,
      type = CreateInvoiceEventMessage.class,
      namespace = TINamespace.MESSAGES
    ),
    @XmlElement(
      name = TIOperation.SCF_PROGRAMME_VALUE,
      type = SCFProgrammeMessage.class,
      namespace = TINamespace.MESSAGES
    )
  })
  private T body;
}
