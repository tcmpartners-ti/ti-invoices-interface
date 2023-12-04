package com.tcmp.tiapi.shared.dto.response.paginated;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginatedResult<T> {
  private List<T> data;

  private PaginatedResultMeta meta;
}
