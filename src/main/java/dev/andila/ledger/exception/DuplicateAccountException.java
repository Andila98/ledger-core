package dev.andila.ledger.exception;

public class DuplicateAccountException extends LedgerException {
  public DuplicateAccountException(String message) {
    super(message);
  }
}
