package com.tcmp.tiapi.shared.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
public class PageParams {
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;

  @Min(value = 0, message = "This field must be greater than or equal 0")
  @Schema(
    description = "Page to request (zero-based counting).",
    defaultValue = "0",
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  private int page = DEFAULT_PAGE;

  @Min(value = 10, message = "This field must be greater than 10")
  @Max(value = 50, message = "This field must be less than or equal 50")
  @Schema(
    description = "Page size.",
    defaultValue = "10",
    requiredMode = RequiredMode.NOT_REQUIRED
  )
  private int size = DEFAULT_SIZE;
}
