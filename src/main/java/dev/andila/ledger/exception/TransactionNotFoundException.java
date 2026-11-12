package dev.andila.ledger.exception;

public class TransactionNotFoundException extends LedgerException {
  public TransactionNotFoundException(String message) {
    super(message);
  }
}
