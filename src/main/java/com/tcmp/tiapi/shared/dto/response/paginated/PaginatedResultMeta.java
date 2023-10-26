package com.tcmp.tiapi.shared.dto.response.paginated;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record PaginatedResultMeta(
  @Schema(description = "True if current page is last page.")
  boolean isLastPage,
  @Schema(description = "Amount of total pages.")
  int totalPages,
  @Schema(description = "Amount of total items.")
  long totalItems
) {
  public static PaginatedResultMeta from(Page<?> page) {
    return PaginatedResultMeta.builder()
      .isLastPage(page.isLast())
      .totalPages(page.getTotalPages())
      .totalItems(page.getTotalElements())
      .build();
  }
}
