package com.tcmp.tiapi.ti.dto.request;

import com.tcmp.tiapi.ti.dto.TINamespace;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@XmlRootElement(name = "ItemRequest", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemRequest {
  @XmlElement(name = "ServiceRequest")
  private ServiceRequest<?> serviceRequest;
}
