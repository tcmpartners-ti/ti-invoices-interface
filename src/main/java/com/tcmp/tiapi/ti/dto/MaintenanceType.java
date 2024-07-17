package com.tcmp.tiapi.ti.dto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.RequiredArgsConstructor;

@XmlJavaTypeAdapter(MaintenanceTypeAdapter.class)
@RequiredArgsConstructor
public enum MaintenanceType {
  DEFINE("F"),
  INSERT("A"),
  UPDATE("U"),
  DELETE("D");

  private final String value;

  public String value() {
    return value;
  }
}

class MaintenanceTypeAdapter extends XmlAdapter<String, MaintenanceType> {
  @Override
  public MaintenanceType unmarshal(String s) {
    return switch (s) {
      case "F" -> MaintenanceType.DEFINE;
      case "A" -> MaintenanceType.INSERT;
      case "U" -> MaintenanceType.UPDATE;
      case "D" -> MaintenanceType.DELETE;
      default -> null;
    };
  }

  @Override
  public String marshal(MaintenanceType type) {
    return switch (type) {
      case DEFINE -> "F";
      case INSERT -> "A";
      case UPDATE -> "U";
      case DELETE -> "D";
    };
  }
}
