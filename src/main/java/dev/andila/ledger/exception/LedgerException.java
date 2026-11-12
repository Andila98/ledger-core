package dev.andila.ledger.exception;

public abstract class LedgerException extends RuntimeException {
  protected LedgerException(String message) {
    super(message);
  }
}
