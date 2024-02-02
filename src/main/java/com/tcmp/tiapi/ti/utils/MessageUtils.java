package com.tcmp.tiapi.ti.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {
  public static String extractFieldFromMessage(String field, String originalMessage) {
    String openingTag = String.format("<%s>", field);
    String closingTag = String.format("</%s>", field);

    int start = originalMessage.indexOf(openingTag) + openingTag.length();
    int end = originalMessage.indexOf(closingTag);

    if (start == -1 || end == -1 || start > end) {
      throw new FieldNotFoundException(
          String.format("Could not find field '%s' in the provided message.", field));
    }

    return originalMessage.substring(start, end);
  }
}

class FieldNotFoundException extends RuntimeException {
  public FieldNotFoundException(String message) {
    super(message);
  }
}
