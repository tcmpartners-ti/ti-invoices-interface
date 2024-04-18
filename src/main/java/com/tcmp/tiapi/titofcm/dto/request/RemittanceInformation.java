package com.tcmp.tiapi.titofcm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemittanceInformation {
  private String information2;
  private String information3;
  private String information4;
}
