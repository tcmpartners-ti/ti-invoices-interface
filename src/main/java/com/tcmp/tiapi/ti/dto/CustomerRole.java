package com.tcmp.tiapi.ti.dto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(CustomerRoleAdapter.class)
public enum CustomerRole {
  BUYER,
  SELLER
}

class CustomerRoleAdapter extends XmlAdapter<String, CustomerRole> {
  @Override
  public CustomerRole unmarshal(String s) {
    return switch (s) {
      case "B" -> CustomerRole.BUYER;
      case "S" -> CustomerRole.SELLER;
      default -> null;
    };
  }

  @Override
  public String marshal(CustomerRole type) {
    return switch (type) {
      case BUYER -> "B";
      case SELLER -> "S";
    };
  }
}
