package com.tcmp.tiapi.titofcm.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PostalAddress {
  private List<String> addressLine;
  private String addressType;
  private String country;
}
