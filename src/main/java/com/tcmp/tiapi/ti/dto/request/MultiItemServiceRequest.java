package com.tcmp.tiapi.ti.dto.request;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.*;
import java.util.List;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "ServiceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
  ServiceRequest.class,
})
public class MultiItemServiceRequest {
  @XmlAttribute(namespace = TINamespace.SCHEMA)
  private String schemaNamespace;

  @XmlAttribute(name = "xmlns", namespace = TINamespace.CONTROL)
  private String controlNamespace;

  @XmlElement(name = "RequestHeader")
  private RequestHeader header;

  @XmlElement(name = "ItemRequest", namespace = TINamespace.MESSAGES)
  private List<ItemRequest> itemRequests;

  public MultiItemServiceRequest(RequestHeader header, List<ItemRequest> itemRequests) {
    this.header = header;
    this.itemRequests = itemRequests;
  }
}
