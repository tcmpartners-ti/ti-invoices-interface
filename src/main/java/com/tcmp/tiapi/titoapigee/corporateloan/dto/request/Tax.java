package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tcmp.tiapi.shared.serializer.JsonMoneySerializer;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tax {
  private String code;
  private PaymentForm paymentForm;

  @JsonSerialize(using = JsonMoneySerializer.class)
  private BigDecimal rate;

  @JsonSerialize(using = JsonMoneySerializer.class)
  private BigDecimal amount;
}
