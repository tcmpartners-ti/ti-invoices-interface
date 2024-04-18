package com.tcmp.tiapi.titofcm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditorDetails {
  private String creditorName;
  private Account account;
}
