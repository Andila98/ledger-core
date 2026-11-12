package dev.andila.ledger.exception;

public class InvalidAccountException extends LedgerException {
  public InvalidAccountException(String message) {
    super(message);
  }
}
