package com.tcmp.tiapi.ti.dto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class TIBooleanAdapter extends XmlAdapter<String, Boolean> {
  @Override
  public Boolean unmarshal(String s) {
    return "Y".equals(s);
  }

  @Override
  public String marshal(Boolean b) {
    return b.equals(Boolean.TRUE) ? "Y" : "N";
  }
}
