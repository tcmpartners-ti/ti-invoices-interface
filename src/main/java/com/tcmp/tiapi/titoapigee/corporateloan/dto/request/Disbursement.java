package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Disbursement {
  private String accountNumber;
  private String accountType;
  private String bankId;
  private String form;
}
