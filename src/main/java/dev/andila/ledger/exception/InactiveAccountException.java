package dev.andila.ledger.exception;

public class InactiveAccountException extends LedgerException {
  public InactiveAccountException(String message) {
    super(message);
  }
}
