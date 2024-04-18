package com.tcmp.tiapi.titofcm.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {
  private ID id;
  private String type;
  private String currency;
  private String name;
}
