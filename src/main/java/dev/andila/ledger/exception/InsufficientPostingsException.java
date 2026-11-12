package dev.andila.ledger.exception;

public class InsufficientPostingsException extends InvalidTransactionException {
  public InsufficientPostingsException(String message) {
    super(message);
  }
}
