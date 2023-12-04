package com.tcmp.tiapi.invoice.util;

import lombok.Getter;

public class EncodedAccountParser {
  private static final int EXPECTED_ACCOUNT_NUMBER_LENGTH = 12;

  private final String encodedAccount;

  @Getter private String type;
  @Getter private String account;

  /**
   * Builds the account object with the account number stored in a custom field in TI (EXTEVENT).
   *
   * @param encodedAccount The customer account that has the format {@code ^(AH|CC)\d{10}$}.
   */
  public EncodedAccountParser(String encodedAccount) {
    if (encodedAccount == null
        || encodedAccount.trim().length() != EXPECTED_ACCOUNT_NUMBER_LENGTH) {
      throw new IllegalArgumentException("Invalid account.");
    }

    this.encodedAccount = encodedAccount.trim();
    parseAccount();
  }

  private void parseAccount() {
    type = encodedAccount.substring(0, 2);
    account = encodedAccount.substring(2);
  }
}
