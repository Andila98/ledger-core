package dev.andila.ledger.exception;

public class UnbalancedTransactionException extends InvalidTransactionException {
  public UnbalancedTransactionException(String message) {
    super(message);
  }
}
