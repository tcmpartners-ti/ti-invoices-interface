package com.tcmp.tiapi.titofcm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditorAgent {
  private String identifierType;
  private String otherId;
  private String name;
  private PostalAddress postalAddress;
}
