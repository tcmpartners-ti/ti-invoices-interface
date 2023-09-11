package com.tcmp.tiapi.shared.dto.response.paginated;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PaginatedResultMeta(
  @Schema(name = "isLastPage", description = "True if current page is last page.")
  boolean isLastPage,
  @Schema(name = "totalPages", description = "Amount of total pages.")
  int totalPages,
  @Schema(name = "totalItems", description = "Amount of total items.")
  long totalItems
) {
}
