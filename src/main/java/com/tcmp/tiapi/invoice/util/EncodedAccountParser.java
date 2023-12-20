package com.tcmp.tiapi.invoice.util;

import com.tcmp.tiapi.shared.FieldValidationRegex;
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
    if (encodedAccount == null) {
      throw new AccountDecodingException("Account cannot be null.");
    }

    String trimmedAccount = encodedAccount.trim();
    if (trimmedAccount.length() != EXPECTED_ACCOUNT_NUMBER_LENGTH) {
      throw new AccountDecodingException("Invalid account length.");
    }

    if (!hasCorrectFormat(trimmedAccount)) {
      throw new AccountDecodingException("Account is not in format ^(AH|CC)\\d{10}$.");
    }

    this.encodedAccount = trimmedAccount;
    parseAccount();
  }

  private boolean hasCorrectFormat(String encodedAccount) {
    return encodedAccount.matches(FieldValidationRegex.BP_BANK_ACCOUNT);
  }

  private void parseAccount() {
    type = encodedAccount.substring(0, 2);
    account = encodedAccount.substring(2);
  }

  public static class AccountDecodingException extends RuntimeException {
    public AccountDecodingException(String message) {
      super(message);
    }
  }
}
