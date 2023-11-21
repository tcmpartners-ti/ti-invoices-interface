package com.tcmp.tiapi.titoapigee.corporateloan.dto.response;

public record Error(
  String code,
  String message,
  String type
) {
  public static Error empty() {
    return new Error("", "", "INFO");
  }

  // Gaf responds with an error like this if everything is fine `{ code: '', message: '', type: 'INFO' }`
  public boolean hasNoError() {
    return code != null
           && code.isBlank()
           && message != null
           && message.isBlank()
           && type != null
           && type.equals("INFO");
  }
}
