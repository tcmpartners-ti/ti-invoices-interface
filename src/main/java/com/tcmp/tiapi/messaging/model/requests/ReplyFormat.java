package com.tcmp.tiapi.messaging.model.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReplyFormat {
  NONE(ReplyFormat.NONE_VALUE),
  STATUS(ReplyFormat.STATUS_VALUE),
  FULL(ReplyFormat.FULL_VALUE);

  public static final String NONE_VALUE = "NONE";
  public static final String STATUS_VALUE = "STATUS";
  public static final String FULL_VALUE = "FULL";

  private final String value;
}
