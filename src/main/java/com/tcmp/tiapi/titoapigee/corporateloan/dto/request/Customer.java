package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
  private String customerId;
  private String documentNumber;
  private String documentType;
  private String fullName;
}
