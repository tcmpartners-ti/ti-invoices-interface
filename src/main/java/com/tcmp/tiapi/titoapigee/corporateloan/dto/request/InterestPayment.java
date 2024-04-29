package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestPayment {
  private String code;
  private GracePeriod gracePeriod;
}
