package com.tcmp.tiapi.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerInfoDTO {
  @Schema(description = "Counter party mnemonic (RUC).", maxLength = 20)
  private String mnemonic;

  @Schema(description = "Counter party name.", maxLength = 35)
  private String name;

  @Schema(description = "Address line 1.", maxLength = 25)
  private String address;

  @Schema(description = "Counter party email address.", maxLength = 8)
  private String email;

  @Schema private String phone;

  @Schema private AccountDTO account;

  @Schema private SellerToProgramRelationDTO relation;

  @Schema(
      description = "Counter party status. B = Blocked; R = Referred; A = Active",
      maxLength = 1)
  private String status;
}
