package com.tcmp.tiapi.shared.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
public class PageParams {
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;

  @Schema(
    description = "Page to request (zero-based counting).",
    defaultValue = "0",
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  private int page = DEFAULT_PAGE;
  @Schema(
    description = "Page size.",
    defaultValue = "10",
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  private int size = DEFAULT_SIZE;
}
