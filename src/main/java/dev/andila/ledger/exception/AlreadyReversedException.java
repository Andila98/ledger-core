package dev.andila.ledger.exception;

public class AlreadyReversedException extends LedgerException {
  public AlreadyReversedException(String message) {
    super(message);
  }
}
