package com.tcmp.tiapi.program.dto.request;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.customer.dto.response.SellerInfoDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramSellersDTO {
  private ProgramDTO programInfo;

  private PaginatedResult<SellerInfoDTO> sellersInfo;
}
