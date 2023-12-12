package com.tcmp.tiapi.ti;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
  @Override
  public LocalDate unmarshal(String value) {
    return LocalDate.parse(value);
  }

  @Override
  public String marshal(LocalDate value) {
    return value.toString();
  }
}
