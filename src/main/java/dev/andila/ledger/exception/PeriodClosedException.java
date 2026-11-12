package dev.andila.ledger.exception;

public class PeriodClosedException extends LedgerException {
  public PeriodClosedException(String message) {
    super(message);
  }
}
