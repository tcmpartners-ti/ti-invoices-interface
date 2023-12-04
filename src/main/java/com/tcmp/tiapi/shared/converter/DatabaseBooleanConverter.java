package com.tcmp.tiapi.shared.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DatabaseBooleanConverter implements AttributeConverter<Boolean, String> {
  private static final String TRUE_VALUE = "Y";
  private static final String FALSE_VALUE = "N";

  @Override
  public String convertToDatabaseColumn(Boolean attribute) {
    if (attribute == null) return FALSE_VALUE;
    return attribute ? TRUE_VALUE : FALSE_VALUE;
  }

  /** This yoda expression helps to avoid NullPointerExceptions. */
  @Override
  public Boolean convertToEntityAttribute(String value) {
    return TRUE_VALUE.equalsIgnoreCase(value);
  }
}
