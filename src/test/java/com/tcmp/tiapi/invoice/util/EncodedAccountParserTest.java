package com.tcmp.tiapi.invoice.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EncodedAccountParserTest {
  @ParameterizedTest
  @ValueSource(strings = {"AH123", "CA1234567890"})
  void itShouldThrowExceptionIfInvalidValue(String account) {
    assertThrows(
        EncodedAccountParser.AccountDecodingException.class,
        () -> new EncodedAccountParser(account));
  }

  @Test
  void itShouldDecodeAccount() {
    var expectedType = "AH";
    var expectedAccount = "1234567890";

    var encodedAccount = new EncodedAccountParser("AH1234567890");

    assertEquals(expectedType, encodedAccount.getType());
    assertEquals(expectedAccount, encodedAccount.getAccount());
  }
}
