package com.tcmp.tiapi.ti.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/***
 * A class is used here instead of an enum because its values are accessed from annotations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TINamespace {
  public static final String SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";

  public static final String MESSAGES = "urn:messages.service.ti.apps.tiplus2.misys.com";
  public static final String COMMON = "urn:common.service.ti.apps.tiplus2.misys.com";
  public static final String CONTROL = "urn:control.services.tiplus2.misys.com";
  public static final String CUSTOM = "urn:custom.service.ti.apps.tiplus2.misys.com";
}
