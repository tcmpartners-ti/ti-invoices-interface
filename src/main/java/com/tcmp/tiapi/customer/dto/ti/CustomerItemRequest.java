package com.tcmp.tiapi.customer.dto.ti;

import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "ItemRequest", namespace = TINamespace.MESSAGES)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ServiceRequest.class})
public class CustomerItemRequest {
  @XmlElement(name = "ServiceRequest")
  private ServiceRequest<Customer> customerRequest;

  @XmlElement(name = "ServiceRequest")
  private ServiceRequest<Account> accountRequest;
}
