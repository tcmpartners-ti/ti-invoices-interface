package com.tcmp.tiapi.invoice.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

public record InvoiceBulkCreationParams(
        @Pattern(
            regexp = "^(sftp|business-banking)$",
            message = "Must be either sftp or business-banking")
        String channel) {

  @RequiredArgsConstructor
  public enum Channel {
    BUSINESS_BANKING("business-banking"),
    SFTP("sftp");

    private final String value;

    public String value() {
      return value;
    }

    @Override
    public String toString() {
      return value;
    }

    public static Channel fromString(String value) {
      return switch (value) {
        case "business-banking" -> BUSINESS_BANKING;
        case "sftp" -> SFTP;
        default -> throw new IllegalArgumentException("Invalid channel value: " + value);
      };
    }
  }
}
