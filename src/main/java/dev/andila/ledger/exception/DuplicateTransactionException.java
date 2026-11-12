package dev.andila.ledger.exception;

public class DuplicateTransactionException extends LedgerException {
  public DuplicateTransactionException(String message) {
    super(message);
  }
}
