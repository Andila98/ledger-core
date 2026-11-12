package dev.andila.ledger.exception;

public class InvalidTransactionException extends LedgerException {
  public InvalidTransactionException(String message) {
    super(message);
  }
}
