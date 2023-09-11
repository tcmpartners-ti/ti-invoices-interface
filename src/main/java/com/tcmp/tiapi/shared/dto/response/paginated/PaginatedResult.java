package com.tcmp.tiapi.shared.dto.response.paginated;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedResult<T> {
  private List<T> data;

  private PaginatedResultMeta meta;
}
