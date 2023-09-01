package com.tcmp.tiapi.shared.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedResult<T> {
  private List<T> data;

  private PaginatedResultMeta meta;
}
